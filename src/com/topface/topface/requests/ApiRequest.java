package com.topface.topface.requests;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import com.topface.topface.*;
import com.topface.topface.utils.http.ConnectionManager;
import com.topface.topface.utils.http.RequestConnection;

import java.util.Calendar;

public abstract class ApiRequest {
    // Data
    public String ssid;
    public ApiHandler handler;
    public Context context;
    public boolean canceled = false;
    private RequestConnection connection;
    private boolean doNeedAlert;
    private boolean doNeedAuthorize;
    private boolean doNeedResend = true;

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
        }
        //Вот эта штука скорее всего не нужна из-за того что такая проверка теперь идет в любой активити
//         else if ((!Data.isSSID() || (new AuthToken(context)).isEmpty()) && doNeedAuthorize) {
//            if (context != null && !AuthActivity.isStarted()) {
//                Debug.log("SSID and Token is empty, need authorize");
//
//                context.startActivity(new Intent(context, AuthActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK));
//            }
//      }

        else {
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
            SharedPreferences mPreferences = context.getSharedPreferences(Static.PREFERENCES_TAG_SHARED, Context.MODE_PRIVATE);
            if (mPreferences != null) {
                long stopTime = Calendar.getInstance().getTimeInMillis();
                mPreferences.edit().putLong(Static.PREFERENCES_STOP_TIME, stopTime).commit();
            }
        }
    }

    public boolean isNeedResend() {
        return doNeedResend;
    }

    public void setNeedResend(boolean value) {
        doNeedResend = value;
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
}