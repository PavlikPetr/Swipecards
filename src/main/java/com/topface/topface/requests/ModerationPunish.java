package com.topface.topface.requests;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Метод наказания пользователя
 */
public class ModerationPunish extends ApiRequest {
    private static final String SERVICE = "moderation.punish";

    /**
     * идентификатор наказания. ОДЗ: непустая строка
     */
    private String mCode;

    /**
     * идентификатор пользователя. ОДЗ: > 0
     */
    private int mUserId;

    public ModerationPunish(Context context, String code, int userId) {
        super(context);
        mCode = code;
        mUserId = userId;
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        return new JSONObject()
                .put("code", mCode)
                .put("userId", mUserId);
    }

    @Override
    public String getServiceName() {
        return SERVICE;
    }
}
