package com.topface.topface.requests;

import android.content.Context;
import org.json.JSONException;
import org.json.JSONObject;

public class BookmarkDeleteRequest extends ApiRequest {
    public static String SERVICE_NAME = "bookmarkdelete";

    private final int uid;

    public BookmarkDeleteRequest(Context context, int uid) {
        super(context);
        this.uid = uid;
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
