package com.topface.topface.requests;

import android.content.Context;
import org.json.JSONException;
import org.json.JSONObject;

public class RegistrationTokenRequest extends AbstractApiRequest {
    // Data
    public static final String service = "registrationToken";
    public String token; //Токен регистрации в C2DM

    public RegistrationTokenRequest(Context context) {
        super(context);
        doNeedAlert(false);
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        return new JSONObject().put("token", token);
    }

    @Override
    protected String getServiceName() {
        return service;
    }

}
