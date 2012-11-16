package com.topface.topface.requests;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import com.topface.topface.Data;
import com.topface.topface.R;
import com.topface.topface.RetryDialog;
import com.topface.topface.Static;
import com.topface.topface.ui.AuthActivity;
import com.topface.topface.utils.http.ConnectionManager;
import com.topface.topface.utils.http.Http;
import com.topface.topface.utils.http.RequestConnection;
import com.topface.topface.utils.social.AuthToken;

public abstract class ApiRequest {
    // Data
    public String ssid;
    public ApiHandler handler;
    public Context context;
    public boolean canceled = false;
    private RequestConnection connection;
    private boolean doNeedAlert;
    private boolean doNeedAuthorize;

    public ApiRequest(Context context) {
        ssid = Static.EMPTY;
        this.context = context;
        doNeedAlert = true;
        doNeedAuthorize = true;
    }

    public ApiRequest callback(ApiHandler handler) {
        this.handler = handler;
        return this;
    }

    public void exec() {
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
                    handler.fail(0, new ApiResponse(""));
                }
            });

            retryDialog.show();
        } else if((!Data.isSSID() || (new AuthToken(context)).isEmpty()) && doNeedAuthorize) {
            context.startActivity(new Intent(context, AuthActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK));
        } else {
            connection = ConnectionManager.getInstance().sendRequest(this);
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
        if (connection != null)
            connection.abort();
        canceled = true;
    }
}