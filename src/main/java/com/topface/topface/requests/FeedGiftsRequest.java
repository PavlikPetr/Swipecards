package com.topface.topface.requests;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

public class FeedGiftsRequest extends ApiRequest {
    // Data
    public static final String service = "gift.getList";
    public int limit; // максимальный размер выбираемых подарков
    public int from = -1; // идентификатор подарка, от которого делать выборку    
    public int uid; // идентификатор пользователя для выборки подарков.

    public FeedGiftsRequest(Context context) {
        super(context);
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        JSONObject result = new JSONObject().put("limit", 7).put("userId", uid);
        if (from != -1) result.put("to", from);
        return result;
    }

    @Override
    public String getServiceName() {
        return service;
    }
}
