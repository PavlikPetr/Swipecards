package com.topface.topface.requests;


import android.content.Context;

import com.adjust.sdk.AdjustAttribution;
import com.topface.framework.JsonUtils;

import org.json.JSONException;
import org.json.JSONObject;

public class ReferrerRequest extends ApiRequest {
    public static final String SERVICE_NAME = "referral.track";

    private AdjustAttribution mAttribution;

    public ReferrerRequest(Context context, AdjustAttribution attribution) {
        super(context);
        mAttribution = attribution;
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        return new JSONObject().put("adjust", JsonUtils.toJson(mAttribution));
    }

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }
}
