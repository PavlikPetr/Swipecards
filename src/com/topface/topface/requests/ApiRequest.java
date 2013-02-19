package com.topface.topface.requests;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import com.topface.topface.App;
import com.topface.topface.RetryDialog;
import com.topface.topface.Ssid;
import com.topface.topface.Static;
import com.topface.topface.requests.handlers.ApiHandler;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.http.ConnectionManager;
import com.topface.topface.utils.http.HttpUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.UUID;

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

    // Data
    private String mId;
    public String ssid;
    public ApiHandler handler;
    public Context context;
    public boolean canceled = false;
    protected HttpURLConnection mURLConnection;
    private boolean doNeedAlert;
    private int mResendCnt = 0;
    private String mPostData;

    public ApiRequest(Context context) {
        //Нельзя передавать Application Context!!!! Только контекст Activity
        ssid = Static.EMPTY;
        this.context = context;
        doNeedAlert = true;
    }

    public ApiRequest callback(ApiHandler handler) {
        this.handler = handler;
        this.handler.setContext(context);
        return this;
    }

    @Override
    public void exec() {
        setStopTime();
        setEmptyHandler();

        if (context != null && !App.isOnline() && doNeedAlert) {
            RetryDialog retryDialog = new RetryDialog(context, this);
            handler.fail(0, new ApiResponse(ApiResponse.ERRORS_PROCCESED, "App is offline"));
            retryDialog.show();
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
        Debug.error("Try resend request #" + mResendCnt);

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
        if (handler != null) {
            handler.cancel();
        }
        setFinished();
    }

    private void setStopTime() {
        if (context != null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    SharedPreferences preferences = context.getSharedPreferences(
                            Static.PREFERENCES_TAG_SHARED,
                            Context.MODE_PRIVATE
                    );
                    if (preferences != null) {
                        preferences.edit().putLong(
                                Static.PREFERENCES_STOP_TIME,
                                System.currentTimeMillis()
                        ).commit();
                    }
                }
            }).start();
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
        if (mURLConnection != null) {
            mURLConnection.disconnect();
        }
        mPostData = null;
        handler = null;
        mURLConnection = null;
        canceled = true;
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
        //Если SSID изменился, то сбрасываем кэш данных запроса
        if (!TextUtils.equals(ssid, this.ssid)) {
            mPostData = null;
        }
        this.ssid = ssid;
    }

    @Override
    public String getId() {
        if (mId == null) {
            mId = getRequestId();
        }

        return mId;
    }

    private String getRequestId() {
        return UUID.randomUUID().toString();
    }

    protected JSONObject getRequest() {
        JSONObject root = new JSONObject();
        try {
            root.put("id", getId());
            root.put("service", getServiceName());
            root.put("ssid", ssid);
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

    public HttpURLConnection openConnection() throws IOException {
        //Если открываем новое подключение, то старое закрываем
        if (mURLConnection != null) {
            mURLConnection.disconnect();
        }

        mURLConnection = HttpUtils.openPostConnection(getApiUrl(), CONTENT_TYPE);
        return mURLConnection;
    }

    public void closeConnection() {
        if (mURLConnection != null) {
            mURLConnection.disconnect();
            mURLConnection = null;
        }
    }

    public HttpURLConnection getConnection() throws IOException {
        if (mURLConnection == null) {
            mURLConnection = openConnection();
        }

        return mURLConnection;
    }

    protected String getApiUrl() {
        return Static.API_URL;
    }

    @Override
    public int sendRequest() throws IOException {
        HttpURLConnection connection = getConnection();
        //Непосредственно перед отправкой запроса устанавливаем новый SSID
        setSsid(Ssid.get());
        //Ставим, если это нужно, ревизию (только на тестовых платформах)
        setRevisionHeader(connection);

        //Формируем свои данные для отправки POST запросом
        String requestJson = toPostData();
        //Переводим строку запроса в байты
        byte[] requestData = requestJson.getBytes();
        Debug.logJson(
                ConnectionManager.TAG,
                "REQUEST >>> " + Static.API_URL + " rev:" + getRevNum(),
                requestJson
        );

        //Отправляем наш  POST запрос
        HttpUtils.sendPostData(requestData, connection);

        //Возвращаем HTTP статус ответа
        return connection.getResponseCode();
    }

    @Override
    public String readRequestResult() throws IOException {
        return HttpUtils.readStringFromConnection(getConnection());
    }

    /**
     * Добавляет в http запрос куки с номером ревизии для тестирования беты
     *
     * @param connection соединение к которому будет добавлен заголовок
     */
    protected void setRevisionHeader(HttpURLConnection connection) {
        String rev = getRevNum();
        if (rev != null && rev.length() > 0) {
            connection.setRequestProperty("Cookie", "revnum=" + rev + ";");
        }
    }

    protected static String getRevNum() {
        return App.DEBUG ? Static.REV : "";
    }

}