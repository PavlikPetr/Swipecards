package com.topface.topface.requests;

import android.content.Context;

import com.google.analytics.tracking.android.EasyTracker;

import org.json.JSONException;
import org.json.JSONObject;

public class RestorePwdRequest extends ApiRequest {
    private static final String SERVICE = "register.restorePassword";

    public String login;

    public RestorePwdRequest(Context context) {
        super(context);
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        JSONObject data = new JSONObject();
        data.put("login", login);
        return data;
    }

    @Override
    public String getServiceName() {
        return SERVICE;
    }

    @Override
    public void exec() {
        super.exec();
        EasyTracker.getTracker().sendEvent("Profile", "Auth", "Restore Password", 1L);
    }

    @Override
    public boolean isNeedAuth() {
        return false;
    }
}
