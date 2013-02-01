package com.topface.topface.requests;


import android.content.Context;
import org.json.JSONException;
import org.json.JSONObject;

public class ChangeLoginRequest extends AbstractApiRequest{
    public static final String LOGIN_FIELD_NAME = "login";
    private static String SERVICE_NAME = "change";

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
