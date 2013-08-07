package com.topface.topface.requests;

import android.content.Context;
import android.os.Message;
import android.text.TextUtils;
import com.topface.topface.App;
import com.topface.topface.RetryDialog;
import com.topface.topface.Ssid;
import com.topface.topface.Static;
import com.topface.topface.requests.handlers.ApiHandler;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.Editor;
import com.topface.topface.utils.http.ConnectionManager;
import com.topface.topface.utils.http.HttpUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;

public abstract class ApiRequest implements IApiRequest {
    /**
     * Максимальное количество попыток отправить запрос
     */
    public static final int MAX_RESEND_CNT = 4;
    /**
     * Задержка между попытками
     */
    public static final int RESEND_WAITING_TIME = 2000;

    /**
     * Mime type наших запросов к серверу
     */
    public static final String CONTENT_TYPE = "application/json";
    public static final String APP_IS_OFFILINE = "App is offiline";

    public String ssid;
    public ApiHandler handler;
    public Context context;
    public boolean canceled = false;
    protected HttpURLConnection mURLConnection;
    private boolean doNeedAlert;
    private int mResendCnt = 0;
    private String mPostData;
    protected String mApiUrl;
    private boolean isNeedCounters = true;

    public ApiRequest(Context context) {
        //Нельзя передавать Application Context!!!! Только контекст Activity
        if (isNeedAuth()) {
            ssid = Static.EMPTY;
        }
        this.context = context;
        doNeedAlert = true;
    }

    public ApiRequest callback(ApiHandler handler) {
        if (handler != null) {
            this.handler = handler;
            this.handler.setContext(context);
        }
        return this;
    }

    @Override
    public void exec() {
        setEmptyHandler();
        handler.setNeedCounters(isNeedCounters);

        if (context != null && !App.isOnline() && doNeedAlert) {
            RetryDialog retryDialog = new RetryDialog(context, this);
            if (handler != null) {
                Message msg = new Message();
                msg.obj = new ApiResponse(ApiResponse.ERRORS_PROCCESED, APP_IS_OFFILINE);
                handler.sendMessage(msg);
            }
            try {
                retryDialog.show();
            } catch (Exception e) {
                Debug.error(e);
            }
        } else {
            ConnectionManager.getInstance().sendRequest(this);
        }
    }

    /**
     * Переотправляем запрос
     *
     * @return номер повторной попытки
     */
    @Override
    public int resend() {
        if (handler != null) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    exec();
                }
            }, ApiRequest.RESEND_WAITING_TIME);
        }

        mResendCnt++;
        Debug.error("Try resend request #" + getId() + " try #" + mResendCnt);

        return mResendCnt;
    }

    public void setEmptyHandler() {
        if (handler == null) {
            handler = new ApiHandler() {
                @Override
                public void success(ApiResponse response) {
                }

                @Override
                public void fail(int codeError, ApiResponse response) {
                }
            };
            handler.setContext(context);
        }
    }

    protected void doNeedAlert(boolean value) {
        doNeedAlert = value;
    }

    @Override
    public void cancel() {
        setFinished();
        canceled = true;
        if (handler != null) {
            handler.cancel();
        }
    }

    @Override
    public boolean isCanResend() {
        return handler != null && mResendCnt < MAX_RESEND_CNT;
    }

    @Override
    public boolean isCanceled() {
        return canceled;
    }

    @Override
    public void setFinished() {
        closeConnection();
        mPostData = null;
    }

    @Override
    public String toPostData() {
        if (mPostData == null) {
            mPostData = getRequest().toString();
        }
        return mPostData;
    }

    @Override
    public Context getContext() {
        return context;
    }

    @Override
    public ApiHandler getHandler() {
        return handler;
    }

    @Override
    public void setSsid(String ssid) {
        if (isNeedAuth()) {
            //Если SSID изменился, то сбрасываем кэш данных запроса
            if (!TextUtils.equals(ssid, this.ssid)) {
                mPostData = null;
            }
            this.ssid = ssid;
        }
    }

    @Override
    public String getId() {
        return hashCode() + ".." + mResendCnt;
    }

    protected JSONObject getRequest() {
        JSONObject root = new JSONObject();
        try {
            root.put("id", getId());
            root.put("service", getServiceName());
            if (isNeedAuth()) {
                root.put("ssid", ssid);
            }
            JSONObject data = getRequestData();
            if (data != null) {
                root.put("data", data);
            }
        } catch (JSONException e) {
            Debug.log(this, "Wrong request compiling: " + e);
        }

        return root;
    }

    protected abstract JSONObject getRequestData() throws JSONException;

    public abstract String getServiceName();

    @Override
    final public String toString() {
        return toPostData();
    }

    protected void handleFail(int errorCode, String errorMessage) {
        handler.response(new ApiResponse(errorCode, errorMessage));
    }

    protected HttpURLConnection openConnection() throws IOException {
        //Если открываем новое подключение, то старое закрываем
        closeConnection();

        mURLConnection = HttpUtils.openPostConnection(mApiUrl, getContentType());
        setRevisionHeader(mURLConnection);

        return mURLConnection;
    }

    protected String getContentType() {
        return CONTENT_TYPE;
    }

    public void closeConnection() {
        if (mURLConnection != null) {
            mURLConnection.disconnect();
            mURLConnection = null;
        }
    }

    public HttpURLConnection getConnection() throws IOException {
        if (mURLConnection != null) {
            closeConnection();
        }

        mURLConnection = openConnection();

        return mURLConnection;
    }

    protected String getApiUrl() {
        return App.getConfig().getApiUrl();
    }

    @Override
    final public int sendRequest() throws Exception {
        mApiUrl = getApiUrl();
        HttpURLConnection connection = getConnection();
        if (connection != null) {
            //Непосредственно перед отправкой запроса устанавливаем новый SSID
            setSsid(Ssid.get());
            //Непосредственно пишим данные в подключение
            if (writeData(connection)) {
                //Возвращаем HTTP статус ответа
                return getResponseCode(connection);
            } else {
                //Если не удалось записать данные, то пишем ошибку запроса
                return -1;
            }
        } else {
            Debug.error("CM: getConnection() return null");
            return -1;
        }

    }

    protected boolean writeData(HttpURLConnection connection) throws IOException {
        //Формируем свои данные для отправки POST запросом
        String requestJson = toPostData();

        //Переводим строку запроса в байты
        byte[] requestData = requestJson.getBytes();
        if (requestData.length > 0 && !isCanceled()) {
            Debug.logJson(
                    ConnectionManager.TAG,
                    "REQUEST >>> " + mApiUrl + " rev:" + getRevNum(),
                    requestJson
            );

            //Отправляем наш  POST запрос
            HttpUtils.sendPostData(requestData, connection);

            return true;
        } else {
            Debug.error(String.format("ConnectionManager: Api request %s is empty", getServiceName()));
            return false;
        }
    }

    protected int getResponseCode(HttpURLConnection connection) {
        int responseCode = -1;
        //Мы не хотим что бы при ошибке получения ответа от сервера происходила внутренняя ошибка,
        //иначе запрос не отправится еще раз
        try {
            responseCode = connection.getResponseCode();
        } catch (IOException e) {
            Debug.error("Get response code exception: " + e.toString());
        }
        return responseCode;
    }

    @Override
    public String readRequestResult() throws IOException {
        String result = null;
        if (mURLConnection != null) {
            result = HttpUtils.readStringFromConnection(mURLConnection);
            closeConnection();
        }
        return result;
    }

    /**
     * Добавляет в http запрос куки с номером ревизии для тестирования беты
     *
     * @param connection соединение к которому будет добавлен заголовок
     */
    protected void setRevisionHeader(HttpURLConnection connection) {
        if (App.DEBUG || Editor.isEditor()) {
            String rev = getRevNum();
            if (rev != null && rev.length() > 0) {
                connection.setRequestProperty("Cookie", "revnum=" + rev + ";");
            }
        }
    }

    protected static String getRevNum() {
        return App.getConfig().getApiRevision();
    }

    @Override
    public IApiResponse constructApiResponse(String response) {
        return new ApiResponse(response);
    }

    @Override
    public IApiResponse constructApiResponse(int code, String message) {
        return new ApiResponse(code, message);
    }

    @Override
    public boolean isNeedAuth() {
        return true;
    }

    protected void setNeedCounters(boolean value) {
        isNeedCounters = value;
    }
}