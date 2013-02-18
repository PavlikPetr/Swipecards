package com.topface.topface.requests;

import android.content.Context;
import android.content.SharedPreferences;
import com.topface.topface.App;
import com.topface.topface.RetryDialog;
import com.topface.topface.Static;
import com.topface.topface.utils.http.ConnectionManager;
import com.topface.topface.utils.http.RequestConnection;

public abstract class ApiRequest {
    private static final int MAX_RESEND_CNT = 4;
    // Data
    public String ssid;
    public ApiHandler handler;
    public Context context;
    public boolean canceled = false;
    private RequestConnection connection;
    private boolean doNeedAlert;
    private boolean doNeedAuthorize;
    private int mResendCnt = 0;

    public ApiRequest(Context context) {
        //Нельзя передавать Application Context!!!! Только контекст Activity
        ssid = Static.EMPTY;
        this.context = context;
        doNeedAlert = true;
        doNeedAuthorize = true;
    }

    public ApiRequest callback(ApiHandler handler) {
        this.handler = handler;
        this.handler.setContext(context);
        return this;
    }

    public void exec() {
        setStopTime();
        setHandler();

        if (context != null && !App.isOnline() && doNeedAlert) {
            RetryDialog retryDialog = new RetryDialog(context, this);
            handler.fail(0, new ApiResponse(""));
            retryDialog.show();
        } else {
            connection = ConnectionManager.getInstance().sendRequest(this);
        }

    }

    private void setHandler() {
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

    protected void doNeedAuthorize(boolean value) {
        doNeedAuthorize = value;
    }

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

    public boolean isNeedResend() {
        return mResendCnt < MAX_RESEND_CNT;
    }

    public boolean isCanceled() {
        return canceled;
    }

    public void setFinished() {
        if (connection != null) {
            connection.abort();
        }
        handler = null;
        connection = null;
        canceled = true;
    }

    public int incrementResend() {
        mResendCnt++;
        return mResendCnt;
    }
}