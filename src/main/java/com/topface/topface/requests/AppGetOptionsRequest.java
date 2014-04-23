package com.topface.topface.requests;

import android.content.Context;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by kirussell on 23.04.2014.
 */
public class AppGetOptionsRequest extends ApiRequest {
    private static final String SERVICE_NAME = "app.getOptions";

    public AppGetOptionsRequest(Context context) {
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
