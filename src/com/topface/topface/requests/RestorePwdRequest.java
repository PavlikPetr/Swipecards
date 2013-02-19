package com.topface.topface.requests;

import android.content.Context;
import com.google.analytics.tracking.android.EasyTracker;
import org.json.JSONException;
import org.json.JSONObject;

public class RestorePwdRequest extends ApiRequest {
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
        return "restore";
    }

    @Override
    public void exec() {
        super.exec();
        EasyTracker.getTracker().trackEvent("Profile", "Auth", "Restore Password", 1L);
    }
}
