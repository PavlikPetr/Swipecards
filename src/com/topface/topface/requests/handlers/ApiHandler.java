package com.topface.topface.requests.handlers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.requests.*;
import com.topface.topface.utils.CountersManager;
import com.topface.topface.utils.Debug;
import org.json.JSONObject;

abstract public class ApiHandler extends Handler {

    private Context mContext;
    private boolean mCancel = false;
    private boolean mNeedCounters = true;

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        response((IApiResponse) msg.obj);
    }

    public void response(IApiResponse response) {
        if (!mCancel) {
            try {
                int result = response.getResultCode();
                if (result == ApiResponse.ERRORS_PROCCESED) {
                    fail(ApiResponse.ERRORS_PROCCESED, new ApiResponse(ApiResponse.ERRORS_PROCCESED, "Client exception"));
                } else if (result == ApiResponse.PREMIUM_ACCESS_ONLY) {
                    Debug.error("To do this you have to be a VIP");

                    if (isShowPremiumError()) {
                        //Сообщение о необходимости Премиум-статуса
                        showToast(R.string.general_premium_access_error);
                    }

                    fail(result, response);
                } else if (response.isCodeEqual(IApiResponse.UNCONFIRMED_LOGIN)) {
                    ConfirmedApiRequest.showConfirmDialog(mContext);
                    fail(result, response);
                } else if (result != ApiResponse.RESULT_OK) {
                    fail(result, response);
                } else {
                    if (response instanceof ApiResponse) {
                        setCounters((ApiResponse) response);
                        sendUpdateIntent((ApiResponse) response);
                    }
                    success(response);
                }
            } catch (Exception e) {
                Debug.error("ApiHandler exception", e);
                fail(ApiResponse.ERRORS_PROCCESED, new ApiResponse(ApiResponse.ERRORS_PROCCESED, e.getMessage()));
            }
            try {
                always(response);
            } catch (Exception e) {
                Debug.error("ApiHandler always callback exception", e);
            }
        }
    }

    protected void showToast(final int stringId) {
        if (mContext != null && mContext instanceof Activity) {
            try {
                //показываем уведомление
                Toast.makeText(App.getContext(), stringId, Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Debug.error(e);
            }
        }
    }

    abstract public void success(IApiResponse response);

    abstract public void fail(int codeError, IApiResponse response);

    public void always(IApiResponse response) {
        //Можно переопределить, если вам нужен коллбэк, который выполняется всегда, вне зависимости от результата
    }

    public void cancel() {
        if (!mCancel) {
            always(new ApiResponse());
            mCancel = true;
        }
    }

    private void setCounters(ApiResponse response) {
        if (!mNeedCounters) return;
        try {
            JSONObject counters = response.counters;
            String method = response.method;
            if (counters != null) {
                CountersManager.getInstance(App.getContext())
                        .setMethod(method)
                        .setAllCounters(
                                counters.optInt("unread_likes"),
                                counters.optInt("unread_symphaties"),
                                counters.optInt("unread_messages"),
                                counters.optInt("unread_visitors"),
                                counters.optInt("unread_fans")
                        );
            }
        } catch (Exception e) {
            Debug.error("api handler exception", e);
        }
    }

    private void sendUpdateIntent(ApiResponse response) {
        if (response.method.equals(ProfileRequest.SERVICE_NAME)) {
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent(ProfileRequest.PROFILE_UPDATE_ACTION));
        } else if (response.method.equals(AppOptionsRequest.SERVICE_NAME)) {
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent(AppOptionsRequest.VERSION_INTENT));
        }
    }

    protected boolean hasContext() {
        return mContext != null;
    }

    public void setContext(Context context) {
        mContext = context;
    }

    protected Context getContext() {
        return mContext;
    }

    protected boolean isShowPremiumError() {
        return true;
    }

    protected boolean isCanceled() {
        return mCancel;
    }

    public void setNeedCounters(boolean needCounter) {
        this.mNeedCounters = needCounter;
    }
}
