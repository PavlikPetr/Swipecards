package com.topface.topface.requests;

import android.content.Context;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Реализация метода api options
 */
public class AppOptionsRequest extends ApiRequest {

    public static final String SERVICE_NAME = "user.getAppOptions";
    public static final String VERSION_INTENT = "com.topface.topface.OPTIONS";

    public AppOptionsRequest(Context context) {
        super(context);
        doNeedAlert(false);
    }

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        return null;
    }
}
