package com.topface.topface.requests;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

public class PeopleNearbyRequest extends ApiRequest {
    public static final String SERVICE_NAME = "peopleNearby.getList";
    public double mLat;
    public double mLon;

    public PeopleNearbyRequest(Context context,
                               double lat,
                               double lon) {
        super(context);
        this.mLat = lat;
        this.mLon = lon;
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        JSONObject object = new JSONObject();
        object.put("lat", mLat);
        object.put("lon", mLon);
        return object;
    }

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }
}
