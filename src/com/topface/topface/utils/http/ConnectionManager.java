package com.topface.topface.utils.http;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.RetryDialog;
import com.topface.topface.Ssid;
import com.topface.topface.data.Auth;
import com.topface.topface.requests.AuthRequest;
import com.topface.topface.requests.IApiRequest;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.ui.BanActivity;
import com.topface.topface.ui.fragments.AuthFragment;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.social.AuthToken;

import javax.net.ssl.SSLException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class ConnectionManager {

    /**
     * Количество одновременно выполняемых запросов
     */
    public static final int THREAD_PULL_SIZE = 3;

    private static ConnectionManager mInstanse;
    private ExecutorService mWorker;
    private AtomicBoolean mAuthUpdateFlag;

    public static final String TAG = "ConnectionManager";
    private final HashMap<String, IApiRequest> mPendignRequests;
    private static long mFloodEndsTime = 0;

    private ConnectionManager() {
        mWorker = Executors.newFixedThreadPool(THREAD_PULL_SIZE);
        mAuthUpdateFlag = new AtomicBoolean(false);
        mPendignRequests = new HashMap<String, IApiRequest>();
    }

    public static ConnectionManager getInstance() {
        if (mInstanse == null) {
            mInstanse = new ConnectionManager();
        }
        return mInstanse;
    }


    /**
     * Добавляет запрос в пулл запросов и пытается его выполнить
     *
     * @param apiRequest запрос к серверу
     * @return объект содержащий сам запрос и связанное с ним http соединение
     */
    public boolean sendRequest(final IApiRequest apiRequest) {
        //Если пользователь заблокирован за флуд (или точнее частые запросы к API)
        //То прерываем обработку запроса и показываем предупреждение
        if (checkForFlood(apiRequest)) return false;

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

        if (request == null || request.isCanceled()) {
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
            if (!Ssid.isEmpty() || !request.isNeedAuth()) {
                response = executeRequest(request);
            } else {
                //Если у нас нет авторизационного токена, то выкидываем на авторизацию
                if (AuthToken.getInstance().isEmpty()) {
                    //Если токен пустой, то сразу конструируем ошибку
                    response = request.constructApiResponse(IApiResponse.UNKNOWN_SOCIAL_USER, "AuthToken is empty");
                } else {
                    //Если SSID пустой, то сразу пишим ответ
                    response = request.constructApiResponse(IApiResponse.SESSION_NOT_FOUND, "SSID is empty");
                }

            }

            //Проверяем запрос на ошибку неверной сессии
            if (response.isCodeEqual(IApiResponse.SESSION_NOT_FOUND)) {
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

    private boolean processResponse(IApiRequest apiRequest, IApiResponse apiResponse) {
        boolean needResend = false;
        //Некоторые ошибки обрабатываем дополнительно, не возвращая в клиентский код
        if (apiResponse.isCodeEqual(IApiResponse.BAN)) {
            //Если в результате получили ответ, что забанен, прекращаем обработку, сообщаем об этом
            showBanActivity(apiRequest, apiResponse);
        } else if (apiResponse.isCodeEqual(IApiResponse.DETECT_FLOOD)) {
            //Если пользователь заблокирован за флуд, показываем соответсвующий экран
            showFloodActivity(apiRequest);
        } else if (apiResponse.isCodeEqual(IApiResponse.MAINTENANCE)) {
            //Если на сервере ведуться работы, то показыаем диалог повтора
            needResend = showRetryDialog(apiRequest);
        } else if (isNeedResend(apiResponse)) {
            //Переотправляем запрос, если это возможно
            needResend = resendRequest(apiRequest, apiResponse);
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
        synchronized (mPendignRequests) {
            Debug.log(String.format("add request %s to pending (canceled: %b)", apiRequest.getId(), apiRequest.isCanceled()));
            mPendignRequests.put(apiRequest.getId(), apiRequest);
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
                IApiResponse.NULL_RESPONSE,
                //Если с сервера пришел не корректный json
                IApiResponse.WRONG_RESPONSE,
                //Если после переавторизации у нас все же не верный ssid, то пробуем все повторить
                IApiResponse.SESSION_NOT_FOUND,
                //Если у нас ошибки подключения
                IApiResponse.CONNECTION_ERROR,
                //Если проблема с подключением к социальной сети у сервера
                IApiResponse.NETWORK_CONNECT_ERROR,
                //Если на сервере что-то упало, то пробуем переотправить запрос
                IApiResponse.INTERNAL_SERVER_ERROR
        );
    }

    private void showBanActivity(IApiRequest apiRequest, IApiResponse apiResponse) {
        Intent intent = new Intent(apiRequest.getContext(), BanActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(BanActivity.INTENT_TYPE, BanActivity.TYPE_BAN);
        intent.putExtra(BanActivity.BANNING_TEXT_INTENT, apiResponse.getErrorMessage());
        apiRequest.getContext().startActivity(intent);
    }

    private void showFloodActivity(IApiRequest apiRequest) {
        Intent intent = new Intent(apiRequest.getContext(), BanActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(BanActivity.INTENT_TYPE, BanActivity.TYPE_FLOOD);
        apiRequest.getContext().startActivity(intent);
    }

    private boolean showRetryDialog(final IApiRequest apiRequest) {
        boolean needResend = false;
        final Context context = apiRequest.getContext();
        if (apiRequest.getHandler() != null && context != null && context instanceof Activity) {
            needResend = true;
            apiRequest.getHandler().post(new Runnable() {
                @Override
                public void run() {
                    RetryDialog retryDialog = new RetryDialog(context, apiRequest);
                    retryDialog.setMessage(context.getString(R.string.general_maintenance));
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

    private boolean checkForFlood(IApiRequest apiRequest) {
        // Не посылать запросы пока не истечет время бана за флуд
        if (isBlockedForFlood()) {
            showFloodActivity(apiRequest);
            return true;
        }
        return false;
    }

    private IApiResponse executeRequest(IApiRequest apiRequest) {
        IApiResponse response = null;
        String rawResponse = null;

        try {
            //Отправляем запрос и сразу читаем ответ
            response = apiRequest.sendRequestAndReadResponse();
        } catch (UnknownHostException e) {
            Debug.error(TAG + "::Exception", e);
            //Это ошибка соединение, такие запросы мы будем переотправлять
            response = apiRequest.constructApiResponse(IApiResponse.CONNECTION_ERROR, "Connection exception: " + e.toString());
        } catch (SocketException e) {
            Debug.error(TAG + "::Exception", e);
            //Это ошибка подключения, такие запросы мы будем переотправлять
            response = apiRequest.constructApiResponse(IApiResponse.CONNECTION_ERROR, "Socket exception: " + e.toString());
        } catch (SocketTimeoutException e) {
            Debug.error(TAG + "::Exception", e);
            //Это ошибка подключения, такие запросы мы будем переотправлять
            response = apiRequest.constructApiResponse(IApiResponse.CONNECTION_ERROR, "Socket exception: " + e.toString());
        } catch (SSLException e) {
            Debug.error(TAG + "::Exception", e);
            //Это ошибка соединение, такие запросы мы будем переотправлять
            response = apiRequest.constructApiResponse(IApiResponse.CONNECTION_ERROR, "Connection exception: " + e.toString());
        } catch (Exception e) {
            Debug.error(TAG + "::Exception", e);
            //Это ошибка нашего кода, не нужно автоматически переотправлять такой запрос
            response = apiRequest.constructApiResponse(IApiResponse.ERRORS_PROCCESED, "Request exception: " + e.toString());
        } catch (OutOfMemoryError e) {
            Debug.error(TAG + "::" + e.toString());
            //Если OutOfMemory, то отменяем запросы, толку от этого все равно нет
            response = apiRequest.constructApiResponse(IApiResponse.ERRORS_PROCCESED, "Request OutOfMemory: " + e.toString());
        } finally {
            //Закрываем соединение
            apiRequest.closeConnection();

            if (response == null) {
                response = apiRequest.constructApiResponse(IApiResponse.NULL_RESPONSE, "Null response");
            }
        }

        //Если наш пришли данные от сервера, то логируем их, если нет, то логируем объект запроса
        Debug.logJson(TAG, "RESPONSE <<< Request ID #" + apiRequest.getId(),
                rawResponse != null ? rawResponse : response.toString()
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
                new AuthRequest(AuthToken.getInstance(), context)
        );

        //Проверяем, что авторизация прошла и нет ошибки
        if (authResponse.isCodeEqual(IApiResponse.RESULT_OK)) {
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
        }

        return response;
    }

    private void sendBroadcastReauth(Context context) {
        Intent intent = new Intent();
        intent.setAction(AuthFragment.REAUTH_INTENT);
        context.sendBroadcast(intent);
    }

    private boolean isBlockedForFlood() {
        if (mFloodEndsTime == 0) {
            mFloodEndsTime = App.getConfig().getFloodEndsTime();
        }
        long now = System.currentTimeMillis();
        return mFloodEndsTime > now;

    }

    /**
     * Заново отправляем отложенные запросы
     */
    private void runPendingRequests() {
        synchronized (mPendignRequests) {
            if (mPendignRequests.size() > 0) {
                int size = mPendignRequests.size();
                Debug.log(TAG + "::Run pendign requests " + size);
                //Перебираем все запросы
                Iterator<Map.Entry<String, IApiRequest>> iterator = mPendignRequests.entrySet().iterator();
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

}
