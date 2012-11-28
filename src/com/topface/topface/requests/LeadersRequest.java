package com.topface.topface.requests;

import android.content.Context;
import org.json.JSONException;
import org.json.JSONObject;

public class LeadersRequest extends AbstractApiRequest {

    public static final String SERVICE_NAME = "leaders";

    public LeadersRequest(Context context) {
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
