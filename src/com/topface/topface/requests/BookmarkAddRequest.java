package com.topface.topface.requests;

import android.content.Context;
import org.json.JSONException;
import org.json.JSONObject;

public class BookmarkAddRequest extends ApiRequest {
    private int uid;
    public static final String SERVICE_NAME = "bookmarkadd";

    public BookmarkAddRequest(Context context, int userId) {
        super(context);
        uid = userId;
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        return new JSONObject().put("userid", uid);
    }

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }
}
