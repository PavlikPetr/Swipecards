package com.topface.topface.requests;

import android.content.Context;
import org.json.JSONException;
import org.json.JSONObject;

public class MainRequest extends AbstractApiRequest {
    // Data
    public static final String service = "main";
    public int photoid; // идентификатор фотографии для установки в качестве главной

    public MainRequest(Context context) {
        super(context);
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        return new JSONObject().put("photoid", photoid);
    }

    @Override
    public String getServiceName() {
        return service;
    }
}
