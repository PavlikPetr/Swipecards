package com.topface.topface.requests;

import android.content.Context;
import org.json.JSONException;
import org.json.JSONObject;

public class LeadersRequest extends AbstractApiRequest {
    public LeadersRequest(Context context) {
        super(context);
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        return null;
    }

    @Override
    protected String getServiceName() {
        return "leaders";
    }
}
