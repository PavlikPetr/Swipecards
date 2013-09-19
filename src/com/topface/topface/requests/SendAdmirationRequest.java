package com.topface.topface.requests;

import android.content.Context;
import org.json.JSONException;
import org.json.JSONObject;

public class SendAdmirationRequest extends SendLikeRequest {

    public static final String service = "admiration.send";

    public SendAdmirationRequest(Context context, int userId, Place place) {
        super(context, userId, place);
    }

    public SendAdmirationRequest(Context context, int userId, int mutualId, Place place) {
        super(context, userId, mutualId, place);
    }

    @Override
    public String getServiceName() {
        return service;
    }
}
