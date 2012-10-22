package com.topface.topface.requests;

import android.content.Context;
import org.json.JSONException;
import org.json.JSONObject;

public class SearchRequest extends AbstractApiRequest {
    // Data
    private static final String mServiceName = "search";
    public int limit; // размер получаемой выборки 10 <= limit <= 50
    public boolean geo; // необходимости геопозиционного поиска
    public boolean ero; // флаг необходимости эротического поиска
    public boolean online; // необходимость выборки только онлайн-пользователей

    public SearchRequest(Context context) {
        super(context);
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        return new JSONObject()
                .put("limit", limit)
                .put("geo", geo)
                .put("ero", ero)
                .put("online", online);
    }

    @Override
    protected String getServiceName() {
        return mServiceName;
    }

}
