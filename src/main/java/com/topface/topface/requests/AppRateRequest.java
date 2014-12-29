package com.topface.topface.requests;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

public class AppRateRequest extends ApiRequest {
    private static final String SERVICE_NAME = "app.setRate";

    // zero rating means what user don't want to rate app
    public static final long NO_RATE = 0L;

    private long mRating = NO_RATE;

    public AppRateRequest(Context context, long rating) {
        super(context);
        mRating = rating;
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        return new JSONObject().put("rate", mRating);
    }

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }
}
