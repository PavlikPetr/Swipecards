package com.topface.topface.requests;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

public class SkipClosedRequest extends ApiRequest {

    public static final String service = "search.skipClosed";

    public String item;

    public SkipClosedRequest(Context context) {
        super(context);
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        return new JSONObject().put("item", item);
    }

    @Override
    public String getServiceName() {
        return service;
    }

    @Override
    public void exec() {
        super.exec();
    }
}
