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
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.ConfirmedApiRequest;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.ProfileRequest;
import com.topface.topface.requests.UserGetAppOptionsRequest;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.CountersManager;
import com.topface.topface.utils.Debug;

import org.json.JSONObject;

abstract public class ApiHandler extends Handler {

    private Context mContext;
    private boolean mCancel = false;
    private boolean mNeedCounters = true;
    private CompleteAction mCompleteAction;

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
                    setCounters(response);
                    sendUpdateIntent(response);

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
        if (mCompleteAction != null) {
            mCompleteAction.onCompleteAction();
        }
    }

    public void cancel() {
        if (!mCancel) {
            always(new ApiResponse());
            mCancel = true;
        }
    }

    public void setCancel(boolean value) {
        mCancel = value;
    }

    private void setCounters(IApiResponse response) {
        if (!mNeedCounters || !response.isNeedUpdateCounters()) return;
        try {
            JSONObject unread = response.getUnread();
            String method = response.getMethodName();
            Debug.log("Set counters from method " + method);
            CountersManager countersManager = CountersManager
                    .getInstance(App.getContext())
                    .setMethod(method);

            if (unread != null) {
                countersManager
                        .setEntitiesCounters(
                                unread.optInt("likes"),
                                unread.optInt("mutual"),
                                unread.optInt("dialogs"),
                                unread.optInt("visitors"),
                                unread.optInt("fans"),
                                unread.optInt("admirations"),
                                unread.optInt("peopleNearby"));
            }
            countersManager.setBalanceCounters(response.getBalance());
        } catch (Exception e) {
            Debug.error("api handler exception", e);
        }
    }

    private void sendUpdateIntent(IApiResponse response) {
        String methodName = response.getMethodName();
        if (methodName.equals(ProfileRequest.SERVICE)) {
            CacheProfile.sendUpdateProfileBroadcast();
        } else if (methodName.equals(UserGetAppOptionsRequest.SERVICE)) {
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent(UserGetAppOptionsRequest.VERSION_INTENT));
        }
    }

    protected boolean hasContext() {
        return mContext != null;
    }

    protected Context getContext() {
        return mContext;
    }

    public void setContext(Context context) {
        mContext = context;
    }

    protected boolean isShowPremiumError() {
        return true;
    }

    public void setNeedCounters(boolean needCounter) {
        this.mNeedCounters = needCounter;
    }

    public void setOnCompleteAction(CompleteAction completeAction) {
        mCompleteAction = completeAction;
    }

    public static interface CompleteAction {
        void onCompleteAction();
    }
}
