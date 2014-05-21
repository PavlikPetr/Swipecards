package com.topface.topface.requests.handlers;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.topface.topface.requests.IApiResponse;
import com.topface.topface.ui.ContainerActivity;

/**
 * This handler sends broadcasts every time users added to or deleted from bookmarks or blacklist.
 */
public class AttitudeHandler extends VipApiHandler {

    private Context mContext;
    private ContainerActivity.ActionTypes mActionType;
    private int[] mUserId;
    private boolean mIsAddition;

    public AttitudeHandler(Context context, ContainerActivity.ActionTypes actionType,
                           int[] userId, boolean isAddition) {
        mContext = context;
        mActionType = actionType;
        mUserId = userId;
        mIsAddition = isAddition;
    }

    @Override
    public void success(IApiResponse response) {
        super.success(response);
        Intent intent;
        if (mUserId.length == 1) {
            intent = ContainerActivity.getValuedActionsUpdateIntent(mActionType, mUserId[0], mIsAddition);
        } else {
            intent = ContainerActivity.getValuedActionsUpdateIntent(mActionType, mUserId, mIsAddition);
        }
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

    @Override
    public void fail(int codeError, IApiResponse response) {
        super.fail(codeError, response);
        Intent intent = ContainerActivity.getIntentForActionsUpdate(mActionType, false);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }
}
