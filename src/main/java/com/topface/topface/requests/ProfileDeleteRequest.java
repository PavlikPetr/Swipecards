package com.topface.topface.requests;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

public class ProfileDeleteRequest extends ApiRequest {
    private static final String SERVICE = "user.delete";

    private int reason;
    private String message;

    public ProfileDeleteRequest(int reasonId, String userMessage, Context context) {
        super(context);
        reason = reasonId;
        message = userMessage;
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        JSONObject data = new JSONObject();
        data.put("reason", reason)
                .put("message", message);
        return data;
    }

    @Override
    public String getServiceName() {
        return SERVICE;
    }
}
