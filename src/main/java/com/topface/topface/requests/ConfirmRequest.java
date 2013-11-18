package com.topface.topface.requests;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

public class ConfirmRequest extends ApiRequest {

    private static final String SERVICE_NAME = "register.confirm";
    private static final String LOGIN_FIELD_NAME = "login";
    private static final String CODE_FIELD_NAME = "code";
    private String login;
    private String code;

    public ConfirmRequest(Context context, String login, String code) {
        super(context);
        this.login = login;
        this.code = code;

    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        return new JSONObject().put(LOGIN_FIELD_NAME, login)
                .put(CODE_FIELD_NAME, code);
    }

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }
}
