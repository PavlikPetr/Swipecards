package com.topface.topface.requests;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

public class SearchRequest extends ApiRequest {
    // Data
    public static final String SERVICE_NAME = "search.getList";
    public int limit; // размер получаемой выборки 10 <= limit <= 50
    public boolean online; // необходимость выборки только онлайн-пользователей

    public SearchRequest(Context context) {
        super(context);
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        return new JSONObject()
                .put("limit", limit)
                .put("online", online);
    }

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }

}
