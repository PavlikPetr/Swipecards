package com.topface.topface.requests;

import android.content.Context;
import org.json.JSONException;
import org.json.JSONObject;

public class GooglePlayProductsRequest extends ApiRequest {
    private static final String SERVICE = "googleplay.getProducts";

    public GooglePlayProductsRequest(Context context) {
        super(context);
        doNeedAlert(false);
    }

    @Override
    public String getServiceName() {
        return SERVICE;
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        return null;
    }
}
