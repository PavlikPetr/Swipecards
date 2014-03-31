package com.topface.topface.requests;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

public class SearchRequest extends ApiRequest {
    public static final int SEARCH_LIMIT = 30;
    // Data
    public static final String SERVICE_NAME = "search.getList";
    private int limit; // размер получаемой выборки 10 <= limit <= 50
    private boolean onlyOnline = false; // необходимость выборки только онлайн-пользователей

    public SearchRequest(int limit, boolean onlyOnline, Context context) {
        super(context);
        this.limit = limit;
        this.onlyOnline = onlyOnline;
    }

    public SearchRequest(boolean onlyOnline, Context context) {
        this(SEARCH_LIMIT, onlyOnline, context);
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        return new JSONObject()
                .put("limit", limit)
                .put("online", onlyOnline);
    }

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }

}
