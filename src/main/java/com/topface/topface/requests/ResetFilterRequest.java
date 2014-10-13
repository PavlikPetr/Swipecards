package com.topface.topface.requests;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Request to get new wider filter
 */
public class ResetFilterRequest extends ApiRequest {

    private static final String SERVICE_NAME = "search.expandFilter";

    public ResetFilterRequest(Context context) {
        super(context);
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        return null;
    }

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }
}
