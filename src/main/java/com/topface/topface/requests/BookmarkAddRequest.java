package com.topface.topface.requests;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

public class BookmarkAddRequest extends ConfirmedApiRequest {
    private int uid;
    public static final String SERVICE_NAME = "bookmark.add";
    public static final String UPDATE_BOOKMARKED = "com.topface.topface.action.ADD_TO_BOOKMARK_INTENT";

    public BookmarkAddRequest(int userId, Context context) {
        super(context);
        uid = userId;
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        return new JSONObject().put("userId", uid);
    }

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }
}
