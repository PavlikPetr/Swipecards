package com.topface.topface.requests;

import android.content.Context;
import org.json.JSONException;
import org.json.JSONObject;

public class GiftsRequest extends AbstractApiRequest {

    public static final String service = "gifts";

    public GiftsRequest(Context context) {
        super(context);
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        return null;
    }

    @Override
    public String getServiceName() {
        return service;
    }

}
