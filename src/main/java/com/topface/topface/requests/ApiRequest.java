package com.topface.topface.requests;

import android.app.Activity;
import android.content.Context;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import com.topface.framework.utils.BackgroundThread;
import com.topface.framework.utils.Debug;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.RetryDialog;
import com.topface.topface.Ssid;
import com.topface.topface.requests.handlers.ApiHandler;
import com.topface.topface.requests.handlers.ErrorCodes;
import com.topface.topface.requests.transport.Headers;
import com.topface.topface.requests.transport.HttpApiTransport;
import com.topface.topface.requests.transport.IApiTransport;
import com.topface.topface.requests.transport.scruffy.ScruffyApiTransport;
import com.topface.topface.utils.http.ConnectionManager;

import org.json.JSONException;
import org.json.JSONObject;

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

    public static final int API_VERSION = 7;
    /**
     * Mime type наших запросов к серверу
     */
    public static final String CONTENT_TYPE = "application/json";

    public String ssid;
    public ApiHandler handler;
    public Context context;
    public boolean canceled = false;
    protected HttpURLConnection mURLConnection;
    private boolean doNeedAlert;
    private int mResendCnt = 0;
    private String mPostData;
    private boolean isNeedCounters = true;
    private IApiTransport mDefaultTransport;

    public ApiRequest(Context context) {
        if (isNeedAuth()) {
            ssid = Ssid.get();
        }
        this.context = context;
        doNeedAlert = true;
    }

    @Override
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

        if (context != null && context instanceof Activity && !App.isOnline() && doNeedAlert) {
            RetryDialog retryDialog = new RetryDialog(context.getString(R.string.general_internet_off), context, this);
            if (handler != null) {
                Message msg = new Message();
                msg.obj = new ApiResponse(ErrorCodes.ERRORS_PROCCESED, "App is offline");
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

    public void resetResendCounter() {
        mResendCnt = 0;
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

    @Override
    public void setEmptyHandler() {
        if (handler == null) {
            handler = new ApiHandler(Looper.getMainLooper()) {
                @Override
                public void success(IApiResponse response) {
                }

                @Override
                public void fail(int codeError, IApiResponse response) {
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
        setCanceled(true);
        if (handler != null) {
            handler.cancel();
        }
    }

    private void setCanceled(boolean b) {
        canceled = b;
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
        mPostData = null;
    }

    @Override
    public String getRequestBodyData() {
        //Непосредственно перед отправкой запроса устанавливаем новый SSID
        setSsid(Ssid.get());

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

    public int getResendCounter() {
        return mResendCnt;
    }

    protected boolean setSsid(String ssid) {
        if (isNeedAuth()) {
            //Если SSID изменился, то сбрасываем кэш данных запроса
            if (!TextUtils.equals(ssid, this.ssid)) {
                mPostData = null;
            }
            this.ssid = ssid;
        }
        return true;
    }

    @Override
    public String getId() {
        return hashCode() + ".." + mResendCnt;
    }

    @Override
    public boolean containsAuth() {
        return false;
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

    @Override
    final public String toString() {
        return getRequestBodyData();
    }

    protected void handleFail(int errorCode, String errorMessage) {
        handler.response(new ApiResponse(errorCode, errorMessage));
    }

    /**
     * В KitKat нельзя завершать соединение из ui треда, что нам нужно при отмене запросов
     * Поэтому используем этот
     */
    public void closeConnectionAsync() {
        if (mURLConnection != null) {
            new BackgroundThread() {
                @Override
                public void execute() {
                    if (mURLConnection != null) {
                        //Проблема в KitKat, связаная с багом https://github.com/square/okhttp/issues/658
                        try {
                            mURLConnection.disconnect();
                        } catch (Exception e) {
                            Debug.error(e);
                        } finally {
                            mURLConnection = null;
                        }
                    }
                }
            };
        }
    }

    @Override
    public IApiResponse sendRequestAndReadResponse() throws Exception {
        return getTransport().sendRequestAndReadResponse(this);

    }

    protected IApiTransport getTransport() {
        switch (App.getApiTransport()) {
            case ScruffyApiTransport.TRANSPORT_NAME:
                return new ScruffyApiTransport();
            case HttpApiTransport.TRANSPORT_NAME:
            default:
                return getDefaultTransport();
        }
    }

    protected IApiTransport getDefaultTransport() {
        if (mDefaultTransport == null) {
            mDefaultTransport = new HttpApiTransport();
        }
        return mDefaultTransport;
    }

    @Override
    public String getContentType() {
        return CONTENT_TYPE;
    }

    @Override
    public boolean isNeedAuth() {
        return true;
    }

    protected void setNeedCounters(boolean value) {
        isNeedCounters = value;
    }

    public void sendHandlerMessage(IApiResponse apiResponse) {
        if (handler != null) {
            Message msg = new Message();
            msg.obj = apiResponse;
            handler.sendMessage(msg);
        }
    }

    //Отменяем запрос из UI потока
    public void cancelFromUi() {
        closeConnectionAsync();
        mPostData = null;
        setCanceled(true);
        if (handler != null) {
            handler.cancel();
        }
    }

    public static interface IConnectionConfigureListener {
        void onConfigureEnd();

        void onConnectionEstablished();
    }

    @Override
    public Headers getHeaders() {
        return new Headers(getId(), getContentType());
    }

    @Override
    public RequestBuilder intoBuilder(RequestBuilder requestBuilder) {
        return requestBuilder.singleRequest(this);
    }

    @Override
    public String getApiUrl() {
        return App.getAppConfig().getApiUrl();
    }
}
