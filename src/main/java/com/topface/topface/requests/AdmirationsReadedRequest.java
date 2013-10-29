package com.topface.topface.requests;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

public class AdmirationsReadedRequest extends ApiRequest {
    public String SERVICE_NAME = "admiration.markAllRead";

    public AdmirationsReadedRequest(Context context) {
        super(context);
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        return new JSONObject();
    }

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }
}
