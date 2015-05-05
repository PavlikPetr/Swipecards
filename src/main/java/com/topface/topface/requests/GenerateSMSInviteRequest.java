package com.topface.topface.requests;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

public class GenerateSMSInviteRequest extends ApiRequest {
    public static final String SERVICE_NAME = "virus.generateSmsInvite";
    String mPhoneNumber;

    public GenerateSMSInviteRequest(Context context, String phoneNumber) {
        super(context);
        mPhoneNumber = phoneNumber;
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        JSONObject result = new JSONObject();
        result.put("phone", mPhoneNumber);
        return result;
    }

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }
}
