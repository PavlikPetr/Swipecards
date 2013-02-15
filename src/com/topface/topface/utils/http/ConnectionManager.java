package com.topface.topface.utils.http;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Message;
import android.preference.PreferenceManager;
import com.topface.topface.*;
import com.topface.topface.data.Auth;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.AuthRequest;
import com.topface.topface.requests.IApiRequest;
import com.topface.topface.requests.handlers.ApiHandler;
import com.topface.topface.ui.BanActivity;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.social.AuthToken;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Arrays;
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

    /**
     * Mime type наших запросов к серверу
     */
    public static final String CONTENT_TYPE = "application/json";

    public static final String TAG = "ConnectionManager";
    private final HashMap<String, IApiRequest> mPendignRequests;
    private long mFloodEndsTime = 0;

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
    public RequestConnection sendRequest(final IApiRequest apiRequest) {
        if (checkForFlood(apiRequest)) return null;

        final RequestConnection connection = new RequestConnection(apiRequest);

        mWorker.submit(new Runnable() {
            @Override
            public void run() {
                runRequest(connection);
            }
        });

        return connection;
    }

    private void runRequest(final RequestConnection request) {
        //Флаг, по которому мы будем определять в конце запроса, нужно ли нам затирать запрос и закрывать соедининение
        boolean needResend = false;

        if (request == null || request.getApiRequest().isCanceled()) {
            //Если запрос отменен, то прекращаем обработку сразу
            return;
        } else if (mAuthUpdateFlag.get()) {
            //Если же запрос нового SSID в процессе, то отправляем запрос в список ожидающих авторизации
            addToPendign(request);
            return;
        }

        final IApiRequest apiRequest = request.getApiRequest();

        try {
            //Отправляем запрос
            ApiResponse apiResponse = executeRequest(request);

            //Проверяем запрос на ошибку неверной сессии
            if (apiResponse.code == ApiResponse.SESSION_NOT_FOUND) {
                //если сессия истекла, то переотправляем запрос авторизации в том же потоке
                apiResponse = resendAfterAuth(request);

                //Если после отпправки на авторизацию вернулся пустой запрос,
                //значит другой поток уже отправил запрос авторизации и нам нужно завершаем обработку и ждать новый SSID
                if (apiResponse == null) {
                    return;
                }
            }

            //Проверяем, нет ли в конечном запросе ошибок авторизации (т.е. не верного токена, пароля и т.п.)
            checkAuthError(apiRequest, apiResponse);

            //Обрабатываем ответ от сервера
            needResend = processResponse(apiRequest, apiResponse);

        } catch (Exception e) {
            //Мы отлавливаем все ошибки, возникшие при запросе, не хотим что бы приложение падало из-за них
            Debug.error(TAG + "::REQUEST::ERROR", e);
        } finally {
            //Проверяем, нужно ли завершать запрос и соответсвенно закрыть соединение и почистить запрос
            if (!needResend) {
                //Отмечаем запрос отмененным, что бы почистить
                apiRequest.setFinished();
            }
        }
    }

    private boolean processResponse(IApiRequest apiRequest, ApiResponse apiResponse) {
        boolean needResend = false;
        //Некоторые ошибки обрабатываем дополнительно, не возвращая в клиентский код
        if (apiResponse.code == ApiResponse.BAN) {
            //Если в результате получили ответ, что забанен, прекращаем обработку, сообщаем об этом
            showBanActivity(apiRequest, apiResponse);
        } else if (apiResponse.code == ApiResponse.DETECT_FLOOD) {
            //Если пользователь заблокирован за флуд, показываем соответсвующий экран
            showFloodActivity(apiRequest);
        } else if (apiResponse.code == ApiResponse.MAINTENANCE) {
            //Если на сервере ведуться работы, то показыаем диалог повтора
            needResend = showRetryDialog(apiRequest);
        } else if (isNeedResend(apiResponse)) {
            //Переотправляем запрос, если это возможно
            needResend = resendRequest(apiRequest, apiResponse);
        } else if (!apiRequest.isCanceled()) {
            //Если запрос не отменен и мы обработали все ошибки, то отправляем callback
            needResend = sendHandlerMessage(apiRequest, apiResponse);
        }
        return needResend;
    }

    /**
     * Добавляет запрос в список отложенных
     *
     * @param request запрос к серверу
     */
    private void addToPendign(RequestConnection request) {
        synchronized (mPendignRequests) {
            IApiRequest apiRequest = request.getApiRequest();
            mPendignRequests.put(apiRequest.getId(), apiRequest);
        }
    }

    private boolean checkAuthError(IApiRequest apiRequest, ApiResponse apiResponse) {
        boolean result = false;
        //Эти ошибки могут возникать, если это запрос авторизации
        // или когда наши регистрационные данные устарели (сменился токен, пароль и т.п)
        if (apiResponse.isWrongAuthError()) {
            //Если не удалос залогиниться, сбрасываем ssid и токен целиком
            Ssid.remove();
            AuthToken.getInstance().removeToken();

            //Отправляем запрос на переавторизацию
            sendBroadcastReauth(apiRequest.getContext());

            //Изначальный же запрос отменяем, нам не нужно что бы он обрабатывался дальше
            apiRequest.setFinished();
            result = true;
        }

        return result;
    }

    private boolean sendHandlerMessage(IApiRequest apiRequest, ApiResponse apiResponse) {
        ApiHandler handler = apiRequest.getHandler();
        if (handler != null) {
            Message msg = new Message();
            msg.obj = apiResponse;
            handler.sendMessage(msg);
        }

        return true;
    }

    /**
     * Повторно отправляет запрос, если это возможно
     *
     * @param apiRequest  запрос
     * @param apiResponse ответ сервера
     * @return удалось ли переотправить запрос
     */
    private boolean resendRequest(IApiRequest apiRequest, ApiResponse apiResponse) {
        boolean needResend = false;
        //Пробуем переотправить запрос
        if (apiRequest.isCanResend()) {
            needResend = true;
            apiRequest.resend();
        } else if (!apiRequest.isCanceled()) {
            //Если не удалось, то просто отправляем сообщение об ошибке
            sendHandlerMessage(apiRequest, apiResponse);
        }
        return needResend;
    }

    /**
     * Проверяет код ответа от сервера и возваращает флаг, нужно ли переотправить данный запрос
     *
     * @param apiResponse ответ сервера
     * @return флаг необходимости повтора запроса
     */
    private boolean isNeedResend(ApiResponse apiResponse) {
        return Arrays.asList(
                //Если ответ пустой
                ApiResponse.NULL_RESPONSE,
                //Если с сервера пришел не корректный json
                ApiResponse.WRONG_RESPONSE,
                //Если после переавторизации у нас все же не верный ssid, то пробуем все повторить
                ApiResponse.SESSION_NOT_FOUND
        ).contains(apiResponse.code);
    }

    private void showBanActivity(IApiRequest apiRequest, ApiResponse apiResponse) {
        Intent intent = new Intent(apiRequest.getContext(), BanActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(BanActivity.INTENT_TYPE, BanActivity.TYPE_BAN);
        intent.putExtra(BanActivity.BANNING_TEXT_INTENT, apiResponse.jsonResult.optString("message", ""));
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
        if (apiRequest.getHandler() != null) {
            needResend = true;
            apiRequest.getHandler().post(new Runnable() {
                @Override
                public void run() {
                    RetryDialog retryDialog = new RetryDialog(apiRequest.getContext(), apiRequest);
                    retryDialog.setMessage(apiRequest.getContext().getString(R.string.general_maintenance));
                    retryDialog.setButton(
                            Dialog.BUTTON_POSITIVE,
                            apiRequest.getContext().getString(R.string.general_dialog_retry),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    apiRequest.exec();
                                }
                            }
                    );
                    retryDialog.show();
                }
            });
        }

        return needResend;
    }

    private void setRequestSsid(RequestConnection request) {
        request.getApiRequest().setSsid(Ssid.get());
    }

    private String getApiUrl() {
        return Static.API_URL;
    }

    private boolean checkForFlood(IApiRequest apiRequest) {
        // Не посылать запросы пока не истечет время бана за флуд
        if (isBlockedForFlood()) {
            showFloodActivity(apiRequest);
            return true;
        }
        return false;
    }


    private ApiResponse executeRequest(RequestConnection request) {
        ApiResponse response = null;
        HttpURLConnection connection = null;
        String rawResponse = null;

        try {
            connection = openConnection();
            request.setConnection(connection);
            //Непосредственно перед отправкой запроса устанавливаем новый SSID
            setRequestSsid(request);
            //Ставим, если это нужно, ревизию (только на тестовых платформах)
            setRevisionHeader(connection);

            //ApiRequest должен сам знать как сформировать свои данные для отправки POST запросом
            String requestJson = request.getApiRequest().toPostData();
            //Переводим строку запроса в байты
            byte[] requestData = requestJson.getBytes();
            Debug.logJson(TAG, "REQUEST >>> " + Static.API_URL + " rev:" + getRevNum(), requestJson);

            //Отправляем наш  POST запрос
            HttpUtils.sendPostData(requestData, connection);

            int responseCode = connection.getResponseCode();

            //Проверяем ответ
            if (HttpUtils.isCorrectResponseCode(responseCode)) {
                //Если код ответа верный, то читаем данные из потока и создаем ApiResponse
                rawResponse = HttpUtils.readStringFromConnection(connection);
                response = new ApiResponse(rawResponse);
            } else {
                //Если не верный, то конструируем соответсвующий ответ
                response = new ApiResponse(ApiResponse.WRONG_RESPONSE, "Wrong http response code HTTP/" + responseCode);
            }

        } catch (Exception e) {
            Debug.error(TAG + "::Exception", e);
        } catch (OutOfMemoryError e) {
            Debug.error(TAG + "::" + e.toString());
        } finally {
            if (connection != null) {
                connection.disconnect();
            }

            if (response == null) {
                response = new ApiResponse(ApiResponse.NULL_RESPONSE, "Null response");
            }
        }

        //Если наш пришли данные от сервера, то логируем их, если нет, то логируем
        Debug.logJson(TAG, "RESPONSE <<<",
                rawResponse != null ? rawResponse : response.toString()
        );

        return response;
    }

    private HttpURLConnection openConnection() throws IOException {
        return HttpUtils.openPostConnection(getApiUrl(), CONTENT_TYPE);
    }

    /**
     * Сперва отправляется запрос авторизации, после чего запрос отправляется вновь
     *
     * @param request запрос, который будет выполнен после авторизации
     * @return ответ сервера
     */
    private ApiResponse sendAuthAndExecute(RequestConnection request) {
        Debug.log(TAG + "::Reauth");
        ApiResponse response = null;
        Context context = request.getApiRequest().getContext();

        //Создаем запрос авторизации
        RequestConnection authConnection = new RequestConnection(
                new AuthRequest(AuthToken.getInstance(), context)
        );

        //И отправляе его
        ApiResponse authResponse = executeRequest(authConnection);

        //Проверяем, что авторизация прошла и нет ошибки
        if (authResponse.code == ApiResponse.RESULT_OK) {
            Auth auth = Auth.parse(authResponse);
            //Сохраняем новый SSID в SharedPreferences
            Ssid.save(auth.ssid);

            //Заново отправляем запрос с уже новым SSID
            response = executeRequest(request);
            //После этого выполняем все отложенные запросы
            runPendingRequests();
        } else if (authResponse.isWrongAuthError()) {
            //Пробрасываем ошибку авторизации в основной запрос, может не очень красиво, зато работает
            //Может стоит сделать отдельный, внутренний, тип ошибки
            response = new ApiResponse(authResponse.code, "Auth error: " + authResponse.message);
        }

        return response;
    }

    /**
     * Добавляет в http запрос куки с номером ревизии для тестирования беты
     *
     * @param connection соединение к которому будет добавлен заголовок
     */
    private void setRevisionHeader(HttpURLConnection connection) {
        String rev = getRevNum();
        if (rev != null && rev.length() > 0) {
            connection.setRequestProperty("Cookie", "revnum=" + rev + ";");
        }
    }

    private void sendBroadcastReauth(Context context) {
        Intent intent = new Intent();
        intent.setAction(ReAuthReceiver.REAUTH_INTENT);
        context.sendBroadcast(intent);
    }

    private boolean isBlockedForFlood() {
        if (mFloodEndsTime == 0) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(App.getContext());
            mFloodEndsTime = preferences.getLong(BanActivity.FLOOD_ENDS_TIME, 0L);
        }
        long now = System.currentTimeMillis();
        return mFloodEndsTime > now;

    }

    private String getRevNum() {
        return App.DEBUG ? Static.REV : "";
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
                    }
                }
            }
        }
    }


    private ApiResponse resendAfterAuth(RequestConnection request) {
        ApiResponse resultResponse = null;
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
