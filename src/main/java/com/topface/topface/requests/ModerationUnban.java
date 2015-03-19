package com.topface.topface.requests;

import android.content.Context;
import org.json.JSONException;
import org.json.JSONObject;

public class ModerationUnban extends ApiRequest {

    private static final String SERVICE = "moderation.unban";

    /** идентификатор пользователя, которого нужно разбанить */
    private int userId;

    public ModerationUnban(Context context, int id) {
        super(context);
        this.userId = id;
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        return new JSONObject()
                .put("userId", userId);
    }

    @Override
    public String getServiceName() {
        return SERVICE;
    }
}
