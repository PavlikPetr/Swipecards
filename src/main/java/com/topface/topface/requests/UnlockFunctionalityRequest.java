package com.topface.topface.requests;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

public class UnlockFunctionalityRequest extends ApiRequest {
    private static final String SERVICE = "user.unlockFunctionality";

    private String mType; // тип разблокируемого функционала, возможные варианты: likes, admirations, visitors, fans.

    public UnlockFunctionalityRequest(String type, Context context) {
        super(context);
        mType = type;
        doNeedAlert(false);
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        return new JSONObject()
                .put("type", mType);
    }

    @Override
    public String getServiceName() {
        return SERVICE;
    }
}

