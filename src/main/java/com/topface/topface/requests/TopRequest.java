package com.topface.topface.requests;

import android.content.Context;
import org.json.JSONException;
import org.json.JSONObject;

public class TopRequest extends ApiRequest {
    // Data
    public static final String service = "top";
    public int sex; // пол самых красивых 
    public int city; // город самых красивых

    public TopRequest(Context context) {
        super(context);
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        return new JSONObject().put("sex", sex).put("city", city);
    }

    @Override
    public String getServiceName() {
        return service;
    }
}
