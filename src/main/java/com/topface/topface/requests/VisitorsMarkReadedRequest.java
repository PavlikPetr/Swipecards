package com.topface.topface.requests;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

public class VisitorsMarkReadedRequest extends ApiRequest{
    public static String SERVICE = "visitor.markAllRead";

    public VisitorsMarkReadedRequest(Context context) {
        super(context);
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        return new JSONObject();
    }

    @Override
    public String getServiceName() {
        return SERVICE;
    }
}
