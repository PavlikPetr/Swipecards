package com.topface.topface.requests;

import android.content.Context;
import org.json.JSONException;
import org.json.JSONObject;

public class FeedGiftsRequest extends AbstractApiRequest {
    // Data
    public static final String service = "feedGifts";
    public int limit; // максимальный размер выбираемых подарков
    public int from; // идентификатор подарка, от которого делать выборку    
    public int uid; // идентификатор пользователя для выборки подарков.

    public FeedGiftsRequest(Context context) {
        super(context);
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        return new JSONObject()
                .put("limit", limit)
                .put("userid", uid);
    }

    @Override
    protected String getServiceName() {
        return service;
    }
}
