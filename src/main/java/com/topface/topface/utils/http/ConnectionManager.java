package com.topface.topface.utils.http;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import com.topface.framework.utils.Debug;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.RetryDialog;
import com.topface.topface.Ssid;
import com.topface.topface.data.Auth;
import com.topface.topface.requests.AuthRequest;
import com.topface.topface.requests.IApiRequest;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.handlers.ApiHandler;
import com.topface.topface.requests.handlers.ErrorCodes;
import com.topface.topface.ui.BanActivity;
import com.topface.topface.ui.fragments.AuthFragment;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.social.AuthToken;

import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.net.ssl.SSLException;

public class ConnectionManager {

    /**
     * Количество одновременно выполняемых запросов
     */
    public static final int THREAD_PULL_SIZE = 3;

    private static ConnectionManager mInstance;
    private ExecutorService mWorker;
    private AtomicBoolean mAuthUpdateFlag;

    public static final String TAG = "ConnectionManager";
    private final HashMap<String, IApiRequest> mPendingRequests;
    private AtomicBoolean mStopRequestsOnBan = new AtomicBoolean(false);

    private ConnectionManager() {
        mWorker = getNewExecutorService();
        mAuthUpdateFlag = new AtomicBoolean(false);
        mPendingRequests = new HashMap<>();
    }

    private ExecutorService getNewExecutorService() {
        return Executors.newFixedThreadPool(THREAD_PULL_SIZE, new ApiThreadFactory());
    }

    public static ConnectionManager getInstance() {
        if (mInstance == null) {
            mInstance = new ConnectionManager();
        }
        return mInstance;
    }


    /**
     * Добавляет запрос в пулл запросов и пытается его выполнить
     *
     * @param apiRequest запрос к серверу
     * @return объект содержащий сам запрос и связанное с ним http соединение
     */
    public boolean sendRequest(final IApiRequest apiRequest) {
        //Добавляем поток с запросом в пулл потоков
        mWorker.submit(new Runnable() {
            @Override
            public void run() {
                runRequest(apiRequest);
            }
        });
        return true;
    }

    /**
     * Отправляет переданный в параметре request запрос
     *
     * @param request Запрос к API
     */
    private void runRequest(final IApiRequest request) {
        //Флаг, по которому мы будем определять в конце запроса, нужно ли нам затирать запрос и закрывать соедининение
        boolean needResend = false;

        if (request == null || request.isCanceled() || mStopRequestsOnBan.get()) {
            Debug.log("CM:: request is canceled");
            //Если запрос отменен, то прекращаем обработку сразу
            return;
        } else if (mAuthUpdateFlag.get()) {
            //Если же запрос нового SSID в процессе, то отправляем запрос в список ожидающих авторизации
            addToPendign(request);
            //И тоже прекращаем обработку
            return;
        }

        try {
            IApiResponse response;
            //Отправляем запрос, если есть SSID и Токен или если запрос не требует авторизации
            if (Ssid.isLoaded() || !request.isNeedAuth()) {
                response = executeRequest(request);
            } else {
                //Если у нас нет авторизационного токена, то выкидываем на авторизацию
                if (AuthToken.getInstance().isEmpty()) {
                    //Если токен пустой, то сразу конструируем ошибку
                    response = request.constructApiResponse(ErrorCodes.UNKNOWN_SOCIAL_USER, "AuthToken is empty");
                } else {
                    //Если SSID пустой, то сразу пишим ответ
                    response = request.constructApiResponse(ErrorCodes.SESSION_NOT_FOUND, "SSID is empty");
                }

            }

            //Проверяем запрос на ошибку неверной сессии
            if (response.isCodeEqual(ErrorCodes.SESSION_NOT_FOUND)) {
                //если сессия истекла, то переотправляем запрос авторизации в том же потоке
                response = resendAfterAuth(request);

                //Если после отпправки на авторизацию вернулся пустой запрос,
                //значит другой поток уже отправил запрос авторизации и нам нужно завершаем обработку и ждать новый SSID
                if (response == null) {
                    return;
                }
            }
            //Проверяем, нет ли в конечном запросе ошибок авторизации (т.е. не верного токена, пароля и т.п.)
            checkAuthError(request, response);
            //Обрабатываем ответ от сервера
            needResend = processResponse(request, response);
        } catch (Exception e) {
            //Мы отлавливаем все ошибки, возникшие при запросе, не хотим что бы приложение падало из-за них
            Debug.error(TAG + "::REQUEST::ERROR", e);
        } finally {
            //Проверяем, нужно ли завершать запрос и соответсвенно закрыть соединение и почистить запрос
            if (!needResend) {
                //Отмечаем запрос отмененным, что бы почистить
                request.setFinished();
            }
        }
    }

    private boolean processResponse(final IApiRequest apiRequest, final IApiResponse apiResponse) {
        boolean needResend = false;
        //Некоторые ошибки обрабатываем дополнительно, не возвращая в клиентский код
        if (apiResponse.isCodeEqual(ErrorCodes.BAN)) {
            //Если в результате получили ответ, что забанен, прекращаем обработку, сообщаем об этом
            showBanActivity(apiRequest, apiResponse);
        } else if (apiResponse.isCodeEqual(ErrorCodes.DETECT_FLOOD)) {
            //Если пользователь заблокирован за флуд, показываем соответсвующий экран
            showFloodActivity(apiRequest, apiResponse);
        } else if (apiResponse.isCodeEqual(ErrorCodes.USER_DELETED)) {
            // посылаем callback для изменения состояния экрана, с которого послали запрос
            apiRequest.sendHandlerMessage(apiResponse);
            //Если пользователь удален, показываем соответсвующий экран
            showRestoreAccountActivity(apiRequest);
        } else if (apiResponse.isCodeEqual(ErrorCodes.MAINTENANCE)) {
            //Если на сервере ведуться работы, то показыаем диалог повтора
            needResend = showRetryDialog(apiRequest);
        } else if (isNeedResend(apiResponse)) {
            //Переотправляем запрос, если это возможно
            needResend = resendRequest(apiRequest, apiResponse);
        } else if (apiResponse.isCodeEqual(ErrorCodes.PREMIUM_ACCESS_ONLY) && CacheProfile.premium) {
            // Перезапрашиваем профиль и настройки, т.к. локальный флаг преимиума устарел
            apiRequest.getHandler().post(new Runnable() {
                @Override
                public void run() {
                    App.sendProfileAndOptionsRequests(new ApiHandler() {
                        @Override
                        public void success(IApiResponse response) {
                            // мы были локально премиум и получили ошибку PREMIUM_ACCESS_ONLY при перезапросе
                            // возвращается что мы премиум, следовательно, прокидываем ошибку чтобы не
                            // перепосылать запрос и не зацикливаться
                            if (CacheProfile.premium) {
                                apiRequest.sendHandlerMessage(apiResponse);
                            } else {
                                resendRequest(apiRequest, apiResponse);
                            }
                        }

                        @Override
                        public void fail(int codeError, IApiResponse response) {
                            // прокидываем ответ на основной запрос, чтобы не поломать логику в месте вызова
                            apiRequest.sendHandlerMessage(apiResponse);
                        }
                    });
                }
            });
        } else if (!apiRequest.isCanceled()) {
            //Если запрос не отменен и мы обработали все ошибки, то отправляем callback
            apiRequest.sendHandlerMessage(apiResponse);
            needResend = false;
        }
        return needResend;
    }

    /**
     * Добавляет запрос в список отложенных
     *
     * @param apiRequest запрос к серверу
     */
    private void addToPendign(IApiRequest apiRequest) {
        synchronized (mPendingRequests) {
            Debug.log(String.format("add request %s to pending (canceled: %b)", apiRequest.getId(), apiRequest.isCanceled()));
            mPendingRequests.put(apiRequest.getId(), apiRequest);
        }
    }

    private boolean checkAuthError(IApiRequest request, IApiResponse response) {
        boolean result = false;
        //Эти ошибки могут возникать, если это запрос авторизации
        // или когда наши регистрационные данные устарели (сменился токен, пароль и т.п)
        if (response.isWrongAuthError()) {
            //Если не удалос залогиниться, сбрасываем ssid и токен целиком
            Ssid.remove();
            AuthToken.getInstance().removeToken();

            //Отправляем запрос на переавторизацию
            sendBroadcastReauth(request.getContext());

            //Изначальный же запрос отменяем, нам не нужно что бы он обрабатывался дальше
            result = true;
        }

        return result;
    }

    /**
     * Повторно отправляет запрос, если это возможно
     *
     * @param apiRequest  запрос
     * @param apiResponse ответ сервера
     * @return удалось ли переотправить запрос
     */
    private boolean resendRequest(IApiRequest apiRequest, IApiResponse apiResponse) {
        boolean needResend = false;
        //Пробуем переотправить запрос
        if (apiRequest.isCanResend()) {
            needResend = true;
            apiRequest.resend();
        } else if (!apiRequest.isCanceled()) {
            //Если не удалось, то просто отправляем сообщение об ошибке
            apiRequest.sendHandlerMessage(apiResponse);
        }
        return needResend;
    }

    /**
     * Проверяет код ответа от сервера и возваращает флаг, нужно ли переотправить данный запрос
     *
     * @param apiResponse ответ сервера
     * @return флаг необходимости повтора запроса
     */
    private boolean isNeedResend(IApiResponse apiResponse) {
        return App.isOnline() && apiResponse.isCodeEqual(
                //Если ответ пустой
                ErrorCodes.NULL_RESPONSE,
                //Если с сервера пришел не корректный json
                ErrorCodes.WRONG_RESPONSE,
                //Если после переавторизации у нас все же не верный ssid, то пробуем все повторить
                ErrorCodes.SESSION_NOT_FOUND,
                //Если у нас ошибки подключения
                ErrorCodes.CONNECTION_ERROR,
                //Если проблема с подключением к социальной сети у сервера
                ErrorCodes.NETWORK_CONNECT_ERROR,
                //Если на сервере что-то упало, то пробуем переотправить запрос
                ErrorCodes.INTERNAL_SERVER_ERROR
        );
    }

    private void showBanActivity(IApiRequest apiRequest, IApiResponse apiResponse) {
        if (mStopRequestsOnBan.get()) return;
        shutdown();
        mStopRequestsOnBan.set(true);
        Intent intent = new Intent(apiRequest.getContext(), BanActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(BanActivity.INTENT_TYPE, BanActivity.TYPE_BAN);
        intent.putExtra(BanActivity.BANNING_TEXT_INTENT, apiResponse.getErrorMessage());
        apiRequest.getContext().startActivity(intent);
    }

    private void showRestoreAccountActivity(IApiRequest apiRequest) {
        Intent intent = new Intent(apiRequest.getContext(), BanActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(BanActivity.INTENT_TYPE, BanActivity.TYPE_RESTORE);
        apiRequest.getContext().startActivity(intent);
    }

    private void showFloodActivity(IApiRequest apiRequest, IApiResponse apiResponse) {
        shutdown();
        // открываем экран флуда
        Intent intent = new Intent(apiRequest.getContext(), BanActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(BanActivity.INTENT_TYPE, BanActivity.TYPE_FLOOD);
        if (apiResponse != null
                && apiResponse.getJsonResult() != null
                && apiResponse.getJsonResult().has("remainingTime")) {
            intent.putExtra(BanActivity.INTENT_FLOOD_TIME,
                    apiResponse.getJsonResult().optLong("remainingTime"));
        }
        apiRequest.getContext().startActivity(intent);
    }

    /**
     * Закрываем воркер, чтобы удалить висящие запросы. Создаем новый пулл для обработки запросов
     */
    private void shutdown() {
        mWorker.shutdownNow();
        mWorker = getNewExecutorService();
    }

    private boolean showRetryDialog(final IApiRequest apiRequest) {
        boolean needResend = false;
        final Context context = apiRequest.getContext();
        if (apiRequest.getHandler() != null && context != null && context instanceof Activity) {
            needResend = true;
            apiRequest.getHandler().post(new Runnable() {
                @Override
                public void run() {
                    RetryDialog retryDialog = new RetryDialog(
                            context.getString(R.string.general_maintenance),
                            context,
                            apiRequest
                    );
                    retryDialog.setButton(
                            Dialog.BUTTON_POSITIVE,
                            context.getString(R.string.general_dialog_retry),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    apiRequest.exec();
                                }
                            }
                    );
                    try {
                        retryDialog.show();
                    } catch (Exception e) {
                        Debug.error(e);
                    }
                }
            });
        }

        return needResend;
    }

    private IApiResponse executeRequest(IApiRequest apiRequest) {
        IApiResponse response = null;

        try {
            //Отправляем запрос и сразу читаем ответ
            response = apiRequest.sendRequestAndReadResponse();
        } catch (UnknownHostException | SocketException | SocketTimeoutException e) {
            Debug.error(TAG + "::Exception", e);
            //Это ошибка соединение, такие запросы мы будем переотправлять
            response = apiRequest.constructApiResponse(ErrorCodes.CONNECTION_ERROR, "Connection exception: " + e.toString());
        } catch (SSLException e) {
            Debug.error(TAG + "::Exception", e);
            //Это ошибка SSL соединения, возможно у юзера не правильно установлено время на устройсте
            //такую ошибку следует обрабатывать отдельно, распарсив сообщение об ошибке и уведомив
            //пользователя
            response = apiRequest.constructApiResponse(ErrorCodes.CONNECTION_ERROR, "Connection SSLException: " + e.toString());
        } catch (Exception e) {
            Debug.error(TAG + "::Exception", e);
            //Это ошибка нашего кода, не нужно автоматически переотправлять такой запрос
            response = apiRequest.constructApiResponse(ErrorCodes.ERRORS_PROCCESED, "Request exception: " + e.toString());
        } catch (OutOfMemoryError e) {
            Debug.error(TAG + "::" + e.toString());
            //Если OutOfMemory, то отменяем запросы, толку от этого все равно нет
            response = apiRequest.constructApiResponse(ErrorCodes.ERRORS_PROCCESED, "Request OutOfMemory: " + e.toString());
        } finally {
            if (response == null) {
                response = apiRequest.constructApiResponse(ErrorCodes.NULL_RESPONSE, "Null response");
            }
        }

        //Если наш пришли данные от сервера, то логируем их, если нет, то логируем объект запроса
        Debug.logJson(TAG, "RESPONSE <<< ID #" + apiRequest.getId(),
                response != null ? response.toString() : null
        );

        return response;
    }

    /**
     * Сперва отправляется запрос авторизации, после чего запрос отправляется вновь
     *
     * @param request запрос, который будет выполнен после авторизации
     * @return ответ сервера
     */
    private IApiResponse sendAuthAndExecute(IApiRequest request) {
        Debug.log(TAG + "::Reauth");
        IApiResponse response = null;
        Context context = request.getContext();

        //Отправляем запрос авторизации
        IApiResponse authResponse = executeRequest(
                new AuthRequest(AuthToken.getInstance().getTokenInfo(), context)
        );

        //Проверяем, что авторизация прошла и нет ошибки
        if (authResponse.isCodeEqual(ErrorCodes.RESULT_OK)) {
            Auth auth = new Auth(authResponse);
            //Сохраняем новый SSID в SharedPreferences
            Ssid.save(auth.ssid);
            //Снимаем блокировку
            mAuthUpdateFlag.set(false);
            //Заново отправляем исходный запрос с уже новым SSID
            response = executeRequest(request);
            //После этого выполняем все отложенные запросы
            runPendingRequests();
        } else if (authResponse.isWrongAuthError()) {
            //Пробрасываем ошибку авторизации в основной запрос, может не очень красиво, зато работает
            //Может стоит сделать отдельный, внутренний, тип ошибки
            response = request.constructApiResponse(authResponse.getResultCode(), "Auth error: " + authResponse.getErrorMessage());
        } else if (authResponse.isCodeEqual(ErrorCodes.USER_DELETED)) {
            response = request.constructApiResponse(authResponse.getResultCode(), "Auth error: " + authResponse.getErrorMessage());
        }

        return response;
    }

    private void sendBroadcastReauth(Context context) {
        Intent intent = new Intent();
        intent.setAction(AuthFragment.REAUTH_INTENT);
        context.sendBroadcast(intent);
    }

    /**
     * Заново отправляем отложенные запросы
     */
    private void runPendingRequests() {
        synchronized (mPendingRequests) {
            if (mPendingRequests.size() > 0) {
                int size = mPendingRequests.size();
                Debug.log(TAG + "::Run pendign requests " + size);
                //Перебираем все запросы
                Iterator<Map.Entry<String, IApiRequest>> iterator = mPendingRequests.entrySet().iterator();
                while (iterator.hasNext()) {
                    //Получаем запрос
                    IApiRequest request = iterator.next().getValue();

                    //Удаляем запрос из списка
                    iterator.remove();

                    //Если запрос еще не отменен, то отправляем
                    if (request != null && !request.isCanceled()) {
                        sendRequest(request);
                    } else {
                        String requestId = (request != null) ? request.getId() : "request in null";
                        Debug.log(TAG + "::Pendign request is canceled " + requestId);
                    }
                }
            }
        }
    }

    private IApiResponse resendAfterAuth(IApiRequest request) {
        IApiResponse resultResponse = null;
        //Проверяем, что еще не запущен запрос авторизации
        if (mAuthUpdateFlag.compareAndSet(false, true)) {
            //Отправляем запрос авторизации, после чего будем перезапрашивать ответ
            resultResponse = sendAuthAndExecute(request);
            //Снимаем блокировку
            mAuthUpdateFlag.set(false);
        } else {
            //Если же запрос нового SSID в процессе, то отправляем запрос в список ожидающих авторизации
            addToPendign(request);
        }

        return resultResponse;
    }

    public void onBanActivityFinish() {
        mStopRequestsOnBan.set(false);
    }
}
