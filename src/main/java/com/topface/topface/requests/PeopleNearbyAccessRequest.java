package com.topface.topface.requests;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

public class PeopleNearbyAccessRequest extends ApiRequest{

    public static final String SERVICE_NAME = "peopleNearby.buyAccess";

    public PeopleNearbyAccessRequest(Context context) {
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
