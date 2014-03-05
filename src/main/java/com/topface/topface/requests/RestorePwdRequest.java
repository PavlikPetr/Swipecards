package com.topface.topface.requests;

import android.content.Context;

import com.google.analytics.tracking.android.EasyTracker;
import com.topface.topface.BuildConfig;

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
        return new JSONObject()
                .put("login", login)
                .put("clientType", BuildConfig.BILLING_TYPE.getClientType());
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
