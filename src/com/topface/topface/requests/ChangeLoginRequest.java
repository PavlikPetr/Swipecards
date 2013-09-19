package com.topface.topface.requests;


import android.content.Context;
import org.json.JSONException;
import org.json.JSONObject;

public class ChangeLoginRequest extends ApiRequest {
    public static final String LOGIN_FIELD_NAME = "login";
    public static final String SERVICE_NAME = "register.changeLogin";

    private String login;

    public ChangeLoginRequest(Context context, String login) {
        super(context);
        this.login = login;
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        return new JSONObject().put(LOGIN_FIELD_NAME, login);
    }

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }
}
