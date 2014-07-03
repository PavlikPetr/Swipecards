package com.topface.topface.requests;

import android.content.Context;

import com.topface.topface.requests.handlers.AttitudeHandler;

import org.json.JSONException;
import org.json.JSONObject;

public class BookmarkAddRequest extends ConfirmedApiRequest {
    private int uid;
    public static final String SERVICE_NAME = "bookmark.add";

    public BookmarkAddRequest(int userId, Context context) {
        super(context);
        uid = userId;
        callback(new AttitudeHandler(context, AttitudeHandler.ActionTypes.BOOKMARK, new int[]{userId}, true));
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
