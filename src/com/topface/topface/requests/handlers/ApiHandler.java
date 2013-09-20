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
                if (result == ErrorCodes.ERRORS_PROCCESED) {
                    fail(ErrorCodes.ERRORS_PROCCESED, new ApiResponse(ErrorCodes.ERRORS_PROCCESED, "Client exception"));
                } else if (result == ErrorCodes.PREMIUM_ACCESS_ONLY) {
                    Debug.error("To do this you have to be a VIP");

                    if (isShowPremiumError()) {
                        //Сообщение о необходимости Премиум-статуса
                        showToast(R.string.general_premium_access_error);
                    }

                    fail(result, response);
                } else if (response.isCodeEqual(ErrorCodes.UNCONFIRMED_LOGIN)) {
                    ConfirmedApiRequest.showConfirmDialog(mContext);
                    fail(result, response);
                } else if (result != ErrorCodes.RESULT_OK) {
                    fail(result, response);
                } else {
                    if (response instanceof ApiResponse) {
                        ApiResponse apiResponse = (ApiResponse) response;
                        setCounters(apiResponse);
                        sendUpdateIntent(apiResponse);
                    }
                    success(response);
                }
            } catch (Exception e) {
                Debug.error("ApiHandler exception", e);
                fail(ErrorCodes.ERRORS_PROCCESED, new ApiResponse(ErrorCodes.ERRORS_PROCCESED, e.getMessage()));
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
            JSONObject unread = response.unread;
            JSONObject balance = response.balance;
            String method = response.method;
            if (unread != null) {
                CountersManager.getInstance(App.getContext())
                        .setMethod(method)
                        .setEntitiesCounters(
                                unread.optInt("unread_likes"),
                                unread.optInt("unread_symphaties"),
                                unread.optInt("unread_messages"),
                                unread.optInt("unread_visitors"),
                                unread.optInt("unread_fans"),
                                unread.optInt("unread_admirations")
                        );
            }
            if (balance != null) {
                CountersManager.getInstance(App.getContext()).setMethod(method).setBalanceCounters(
                        balance.optInt("likes"),
                        balance.optInt("money")
                );
            }
        } catch (Exception e) {
            Debug.error("api handler exception", e);
        }
    }

    private void sendUpdateIntent(ApiResponse response) {
        if (response.method.equals(ProfileRequest.SERVICE)) {
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent(ProfileRequest.PROFILE_UPDATE_ACTION));
        } else if (response.method.equals(AppOptionsRequest.SERVICE)) {
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
