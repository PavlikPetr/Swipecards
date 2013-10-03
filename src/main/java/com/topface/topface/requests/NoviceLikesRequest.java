package com.topface.topface.requests;

import android.content.Context;

import org.json.JSONObject;

public class NoviceLikesRequest extends ApiRequest {
    public static final String SERVICE_NAME = "user.becomeNoviceLikes";

    public NoviceLikesRequest(Context context) {
        super(context);
    }

    @Override
    protected JSONObject getRequestData() {
        return null;
    }

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }

}
