package com.topface.topface.requests.handlers;

import android.content.Context;

import com.topface.topface.requests.IApiResponse;

abstract public class ActionMenuHandler extends AttitudeHandler {

    public ActionMenuHandler(Context context, ActionTypes actionType, int[] userId, boolean isAddition) {
        super(context, actionType, userId, isAddition);
    }

    @Override
    public void success(IApiResponse response) {
        super.success(response);
        closeMenu();
    }

    abstract public void closeMenu();
}
