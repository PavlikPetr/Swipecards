package com.topface.topface.requests;

import android.content.Context;


import com.topface.topface.utils.EasyTracker;

import org.json.JSONException;
import org.json.JSONObject;

public class LogoutRequest extends ApiRequest {
    public static final String service = "auth.logout";

    public LogoutRequest(Context context) {
        super(context);
        setNeedCounters(false);
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        return null;
    }

    @Override
    public String getServiceName() {
        return service;
    }

    @Override
    public void exec() {
        super.exec();
        EasyTracker.sendEvent("Profile", "Logout", "", 1L);
    }
}
