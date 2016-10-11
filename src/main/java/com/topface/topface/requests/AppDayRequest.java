package com.topface.topface.requests;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * Created by siberia87 on 10.10.16.
 */

public class AppDayRequest extends ApiRequest {
    public String SERVICE_NAME = "nothing";

    public AppDayRequest(Context context) {
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
