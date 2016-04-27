package com.topface.topface.requests;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * get list of renewed users subscription
 */
public class OkGetRenewalOfSubscriptionsRequest extends ApiRequest {
    public static final String SERVICE_NAME = "ok.getRenewalOfSubscriptions";

    public OkGetRenewalOfSubscriptionsRequest(Context context) {
        super(context);
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        return new JSONObject();
    }

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }
}
