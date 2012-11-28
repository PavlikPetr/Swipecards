package com.topface.topface.requests;

import android.content.Context;
import org.json.JSONException;
import org.json.JSONObject;

public class FeedGiftsRequest extends AbstractApiRequest {
    // Data
    public static final String service = "feedGifts";
    public int limit; // максимальный размер выбираемых подарков
    public int from = -1; // идентификатор подарка, от которого делать выборку    
    public int uid; // идентификатор пользователя для выборки подарков.

    public FeedGiftsRequest(Context context) {
        super(context);
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
    	JSONObject result = new JSONObject().put("limit", limit).put("userid", uid);
    	if (from != -1) result.put("from", from);    	
        return result;
    }

    @Override
    public String getServiceName() {
        return service;
    }
}
