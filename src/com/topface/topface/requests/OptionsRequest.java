package com.topface.topface.requests;

import android.content.Context;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Реализация метода api options
 */
public class OptionsRequest extends AbstractApiRequest {

    public static final String SERVICE_NAME = "optionsa";
    public static final String MAX_VERSION = "max_version";
    public static final String VERSION_INTENT = "com.topface.topface.OPTIONS";

    public OptionsRequest(Context context) {
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
