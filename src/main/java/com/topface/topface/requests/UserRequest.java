package com.topface.topface.requests;

import android.content.Context;

import com.google.analytics.tracking.android.EasyTracker;

import org.json.JSONException;
import org.json.JSONObject;

public class UserRequest extends ApiRequest {
    private static final String SERVICE = "user.getProfile";

    private int userId; // массив id пользователя в топфейсе

    public UserRequest(int uid, Context context) {
        super(context);
        userId = uid;
        doNeedAlert(false);

    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        return new JSONObject()
                .put("userId", userId);
    }

    @Override
    public String getServiceName() {
        return SERVICE;
    }

    @Override
    public void exec() {
        super.exec();
        EasyTracker.getTracker().sendEvent("Profile", "LoadUser", "", 1l);
    }
}
