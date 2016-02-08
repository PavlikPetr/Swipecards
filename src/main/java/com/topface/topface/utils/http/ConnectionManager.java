package com.topface.topface.utils.http;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.topface.framework.utils.Debug;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.Ssid;
import com.topface.topface.data.Profile;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.IApiRequest;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.handlers.ApiHandler;
import com.topface.topface.requests.handlers.ErrorCodes;
import com.topface.topface.ui.BanActivity;
import com.topface.topface.ui.RestoreAccountActivity;
import com.topface.topface.ui.SslErrorActivity;
import com.topface.topface.ui.fragments.AuthFragment;
import com.topface.topface.ui.fragments.BanFragment;
import com.topface.topface.utils.social.AuthToken;

import org.json.JSONObject;

import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;

public class ConnectionManager {

    /**
     * Количество одновременно выполняемых запросов
     */
    public static final int THREAD_PULL_SIZE = 3;
    public static final String API_URL = "https://api.core.tf/";
    public static final String API_500_ERROR_URL = "http://httpstat.us/500";

    private static ConnectionManager mInstance;
    private ExecutorService mWorker;
    private AtomicBoolean mAuthUpdateFlag;

    public static final String TAG = "ConnectionManager";
    private final HashMap<String, IApiRequest> mPendingRequests;
    private AtomicBoolean mStopRequestsOnBan = new AtomicBoolean(false);
    private AuthAssistant mAuthAssistant = new AuthAssistant();

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
        if (mWorker.isShutdown() || mWorker.isTerminated()) {
            apiRequest.cancel();
            return false;
        }
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
    private void runRequest(IApiRequest request) {
        //Флаг, по которому мы будем определять в конце запроса, нужно ли нам затирать запрос и закрывать соедининение
        boolean needResend = false;
        IApiResponse response = null;

        if (request == null || request.isCanceled() || mStopRequestsOnBan.get()) {
            Debug.log("CM:: request is canceled");
            //Если запрос отменен, то прекращаем обработку сразу
            return;
        } else if (mAuthUpdateFlag.get() && request.isNeedAuth()) {
            //Если же запрос нового SSID в процессе, то отправляем запрос в список ожидающих авторизации
            addToPendign(request);
            //И тоже прекращаем обработку
            return;
        }

        try {
            //Отправляем запрос, если есть SSID, и он не просрочен, и Токен или если запрос не требует авторизации
            if ((Ssid.isLoaded() && !Ssid.isOverdue()) || !request.isNeedAuth() || AuthAssistant.isAuthUnacceptable(request)) {
                response = executeRequest(request);
            } else {
                //Если у нас нет авторизационного токена, то выкидываем на авторизацию
                if (AuthToken.getInstance().isEmpty()) {
                    //Если токен пустой, то сразу конструируем ошибку
                    response = new ApiResponse(ErrorCodes.UNKNOWN_SOCIAL_USER, "AuthToken is empty");
                } else {
                    //Если SSID пустой, то добавлем к изначальном запросу запрос авторизации
                    request = mAuthAssistant.precedeRequestWithAuth(request);
                    response = sendOrPend(request);
                }

            }

            if (response != null) {
                //Проверяем запрос на ошибку неверной сессии
                if (response.isCodeEqual(ErrorCodes.SESSION_NOT_FOUND)) {
                    //Добавляем запрос авторизации
                    if (!AuthAssistant.isAuthUnacceptable(request)) {
                        request = mAuthAssistant.precedeRequestWithAuth(request);
                        response = sendOrPend(request);
                    } else {
                        addToPendign(request);
                        runRequest(mAuthAssistant.createAuthRequest());
                        return;
                    }

                    //Если после отпправки на авторизацию вернулся пустой запрос,
                    //значит другой поток уже отправил запрос авторизации и нам нужно завершаем обработку и ждать новый SSID
                    if (response == null) {
                        return;
                    }
                }
                //Проверяем, нет ли в конечном запросе ошибок авторизации (т.е. не верного токена, пароля и т.п.)
                mAuthAssistant.checkAuthError(response);
                //Обрабатываем ответ от сервера
                needResend = processResponse(request, response);
            }
        } catch (Exception e) {
            //Мы отлавливаем все ошибки, возникшие при запросе, не хотим что бы приложение падало из-за них
            Debug.error(TAG + "::REQUEST::ERROR", e);
        } finally {
            //Проверяем, нужно ли завершать запрос и соответсвенно закрыть соединение и почистить запрос
            if ((!needResend && response != null) &&
                    !(response.isCodeEqual(ErrorCodes.SESSION_NOT_FOUND) && AuthAssistant.isAuthUnacceptable(request))) {
                //Отмечаем запрос отмененным, что бы почистить
                request.setFinished();
            }
        }
    }

    private boolean processResponse(final IApiRequest apiRequest, final IApiResponse apiResponse) {
        boolean needResend = false;
        final Profile profile = App.from(App.getContext()).getProfile();
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
        } else if (apiResponse.isCodeEqual(ErrorCodes.PREMIUM_ACCESS_ONLY) && profile.premium) {
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
                            if (profile.premium) {
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
        if (apiResponse.isCodeEqual(ErrorCodes.HTTPS_CERTIFICATE_EXPIRED)) {
            //Показываем пользователю попап о необходимости корректировки даты на устройстве
            Intent intent = new Intent(App.getContext(), SslErrorActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            App.getContext().startActivity(intent);
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
            Debug.log(String.format(Locale.ENGLISH, "add request %s to pending (canceled: %b)", apiRequest.getId(), apiRequest.isCanceled()));
            mPendingRequests.put(apiRequest.getId(), apiRequest);
        }
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
        JSONObject jsonObject = apiResponse.getJsonResult();
        if (jsonObject != null) {
            intent.putExtra(BanFragment.USER_MESSAGE, jsonObject.optString("userMessage"));
            intent.putExtra(BanFragment.BAN_EXPIRE, jsonObject.optLong("banExpire"));
        }
        intent.putExtra(BanActivity.INTENT_TYPE, BanActivity.TYPE_BAN);
        intent.putExtra(BanActivity.BANNING_TEXT_INTENT, apiResponse.getErrorMessage());
        apiRequest.getContext().startActivity(intent);
    }

    private void showRestoreAccountActivity(IApiRequest apiRequest) {
        Intent intent = new Intent(apiRequest.getContext(), RestoreAccountActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
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

    private int getSslErrorCode(SSLException e) {
        int errorCode = ErrorCodes.CONNECTION_ERROR;
        String[] messages = App.getContext().getResources().getStringArray(R.array.ssl_handshake_exception_messages);
        for (String message : messages) {
            if (e.getMessage().toLowerCase(Locale.getDefault()).contains(message.toLowerCase(Locale.getDefault()))) {
                errorCode = ErrorCodes.HTTPS_CERTIFICATE_EXPIRED;
                break;
            }
        }
        return errorCode;
    }

    private boolean showRetryDialog(final IApiRequest apiRequest) {
        boolean needResend = false;
        final Context context = apiRequest.getContext();
        if (apiRequest.getHandler() != null && context != null && context instanceof Activity) {
            needResend = true;
        }

        return needResend;
    }

    private IApiResponse executeRequest(IApiRequest apiRequest) {
        IApiResponse response;
        try {
            //Отправляем запрос и сразу читаем ответ
            response = apiRequest.sendRequestAndReadResponse();
        } catch (UnknownHostException | SocketException | SocketTimeoutException e) {
            Debug.error(TAG + "::HostException", e);
            //Это ошибка соединение, такие запросы мы будем переотправлять
            response = new ApiResponse(ErrorCodes.CONNECTION_ERROR, "Connection exception: " + e.toString());
        } catch (SSLHandshakeException e) {
            Debug.error(TAG + "::SSLHandshakeException", e);
            //Это ошибка SSL соединения, возможно у юзера не правильно установлено время на устройсте
            //такую ошибку следует обрабатывать отдельно, распарсив сообщение об ошибке и уведомив
            //пользователя
            response = new ApiResponse(getSslErrorCode(e), "Connection SSLHandshakeException: " + e.toString());
        } catch (SSLException e) {
            Debug.error(TAG + "::SSLException", e);
            //Прочие ошибки SSL
            response = new ApiResponse(ErrorCodes.CONNECTION_ERROR, "Connection SSLException: " + e.toString());
        } catch (Exception e) {
            Debug.error(TAG + "::Exception", e);
            //Это ошибка нашего кода, не нужно автоматически переотправлять такой запрос
            response = new ApiResponse(ErrorCodes.ERRORS_PROCESSED, "Request exception: " + e.toString());
        } catch (OutOfMemoryError e) {
            Debug.error(TAG + "::OutOfMemory" + e.toString());
            //Если OutOfMemory, то отменяем запросы, толку от этого все равно нет
            response = new ApiResponse(ErrorCodes.ERRORS_PROCESSED, "Request OutOfMemory: " + e.toString());
        }

        if (response == null) {
            Debug.error(new NullPointerException("Null response"));
            response = new ApiResponse(ErrorCodes.NULL_RESPONSE, "Null response");
        }

        //Если наш пришли данные от сервера, то логируем их, если нет, то логируем объект запроса
        Debug.logJson(TAG, "RESPONSE <<< ID #" + apiRequest.getId(),
                response.toString()
        );

        return response;
    }

    void sendBroadcastReauth(Context context) {
        Intent intent = new Intent();
        intent.setAction(AuthFragment.REAUTH_INTENT);
        context.sendBroadcast(intent);
    }

    /**
     * Заново отправляем отложенные запросы
     */
    void runPendingRequests() {
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

    /**
     * Executes request if there are no auth requests in process.
     * Otherwise lefts request pending until authorization is complete.
     *
     * @return response or null if request was left pending
     */
    private IApiResponse sendOrPend(IApiRequest request) {
        IApiResponse resultResponse = null;
        //Проверяем, что еще не запущен запрос авторизации
        if (mAuthUpdateFlag.compareAndSet(false, true)) {
            Debug.log(TAG + "::Reauth");
            resultResponse = executeRequest(request);
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

    AtomicBoolean getAuthUpdateFlag() {
        return mAuthUpdateFlag;
    }
}
