package com.topface.topface.requests;

import android.content.Context;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Реализация метода api options
 */
public class OptionsRequest extends AbstractApiRequest {

    public static final String SERVICE_NAME = "options";

    public OptionsRequest(Context context) {
        super(context);
    }

    @Override
    protected String getServiceName() {
        return SERVICE_NAME;
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        return null;
    }
}
