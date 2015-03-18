package com.topface.topface.requests;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by dkarpushov on 18.03.15.
 */
public class ModerationUnban extends ApiRequest {

    private static final String SERVICE = "moderation.unban";

    /** идентификатор пользователя, которого нужно разбанить */
    private int mUserId;

    public ModerationUnban(Context context, int id) {
        super(context);
        this.mUserId = id;
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        return new JSONObject()
                .put("userId", mUserId);
    }

    @Override
    public String getServiceName() {
        return SERVICE;
    }
}
