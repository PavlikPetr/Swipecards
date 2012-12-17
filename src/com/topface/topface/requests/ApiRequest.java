package com.topface.topface.requests;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import com.topface.topface.Data;
import com.topface.topface.R;
import com.topface.topface.RetryDialog;
import com.topface.topface.Static;
import com.topface.topface.ui.AuthActivity;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.http.ConnectionManager;
import com.topface.topface.utils.http.Http;
import com.topface.topface.utils.http.RequestConnection;
import com.topface.topface.utils.social.AuthToken;

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

        if (!Http.isOnline(context) && doNeedAlert) {
            RetryDialog retryDialog = new RetryDialog(context);
            retryDialog.setMessage(context.getString(R.string.general_internet_off));
            retryDialog.setButton(Dialog.BUTTON_POSITIVE, context.getString(R.string.general_dialog_retry), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    exec();
                }
            });

            retryDialog.setButton(Dialog.BUTTON_NEGATIVE, context.getString(R.string.general_cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
//                    if (handler != null) handler.fail(0, new ApiResponse(""));
                }
            });
            handler.fail(0, new ApiResponse(""));
            retryDialog.show();
        } else if ((!Data.isSSID() || (new AuthToken(context)).isEmpty()) && doNeedAuthorize) {
            if (!AuthActivity.isStarted()) {
                Debug.log("SSID and Token is empty, need authorize");
                context.startActivity(new Intent(context, AuthActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK));
            }
        } else {
            connection = ConnectionManager.getInstance().sendRequest(this);
        }

    }

    private void setHandler() {
        if (handler == null) {
            handler = new ApiHandler() {
                @Override
                public void success(ApiResponse response) {}

                @Override
                public void fail(int codeError, ApiResponse response) {}
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
        handler = null;
        if (connection != null) {
            connection.abort();
        }
        connection = null;
        canceled = true;
    }

    private void setStopTime() {
        SharedPreferences mPreferences = context.getSharedPreferences(Static.PREFERENCES_TAG_SHARED, Context.MODE_PRIVATE);
        if (mPreferences != null) {
            long stopTime = Calendar.getInstance().getTimeInMillis();
            mPreferences.edit().putLong(Static.PREFERENCES_STOP_TIME, stopTime).commit();
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

}