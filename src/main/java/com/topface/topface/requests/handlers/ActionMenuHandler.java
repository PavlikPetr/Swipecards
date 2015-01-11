package com.topface.topface.requests.handlers;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.topface.topface.requests.ApiRequest;
import com.topface.topface.requests.IApiResponse;

abstract public class ActionMenuHandler extends AttitudeHandler {

    private boolean mIsAddition;
    private int mUserId;
    private ActionTypes mActionType;
    private Context mContext;

    public ActionMenuHandler(Context context) {
        super(context, null, null, false);
        mContext = context;
    }

    @Override
    public void success(IApiResponse response) {
        Intent intent = getValuedActionsUpdateIntent(mActionType, mUserId, mIsAddition);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
        closeActionMenu();
    }

    public void setIntentParam(ActionTypes actionTypes, int userId, boolean isAddition) {
        this.mActionType = actionTypes;
        this.mUserId = userId;
        this.mIsAddition = isAddition;
    }

    public void setCallback(AttitudeHandler.ActionTypes actionTypes, int userId, boolean isAddition, ApiRequest request) {
        setIntentParam(actionTypes, userId, isAddition);
        request.callback(this);
    }

    abstract public void closeActionMenu();

}
