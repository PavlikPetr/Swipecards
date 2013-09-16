package com.topface.topface.requests;

import android.content.Context;
import org.json.JSONException;
import org.json.JSONObject;

public class SendAdmirationRequest extends SendLikeRequest {

    public static final String service = "admiration.send";

    public SendAdmirationRequest(Context context) {
        super(context);
    }

    @Override
    public String getServiceName() {
        return service;
    }
}
