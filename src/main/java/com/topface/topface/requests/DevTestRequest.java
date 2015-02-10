package com.topface.topface.requests;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Debug method to get specified errors.
 */
@SuppressWarnings("unused")
public class DevTestRequest extends ApiRequest {

    public static final String SERVICE_NAME = "dev.test";

    public int required = 2;
    public int nonrequired;
    public int error;

    public DevTestRequest(Context context, int errorCode) {
        super(context);
        error = errorCode;
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        JSONObject data = new JSONObject();
        data.put("required", required);
        data.put("nonrequired", nonrequired);
        data.put("error", error);
        return data;
    }

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }
}
