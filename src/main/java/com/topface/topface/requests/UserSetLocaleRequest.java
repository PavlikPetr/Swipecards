package com.topface.topface.requests;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

public class UserSetLocaleRequest extends ApiRequest {

    public static final String SERVICE_NAME = "user.setLocale";
    private final String locale;

    public UserSetLocaleRequest(Context context, String locale) {
        super(context);
        this.locale = locale;
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        return new JSONObject()
                .put("locale", locale);
    }

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }
}
