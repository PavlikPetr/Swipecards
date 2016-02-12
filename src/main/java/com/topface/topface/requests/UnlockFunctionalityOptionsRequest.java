package com.topface.topface.requests;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

public class UnlockFunctionalityOptionsRequest extends ApiRequest {
    private static final String SERVICE = "user.getUnlockFunctionalityOptions";

    public UnlockFunctionalityOptionsRequest(Context context) {
        super(context);
        doNeedAlert(false);
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        return new JSONObject();
    }

    @Override
    public String getServiceName() {
        return SERVICE;
    }
}

