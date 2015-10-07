package com.topface.topface.requests;


import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Запрос на востановление пароля с экрана авторизации
 * Created by onikitin on 14.09.15.
 */
public class ChangePwdFromAuthRequest extends ApiRequest {

    public static final String SERVICE_NAME = "register.confirmPasswordRestoration";
    private String mHash;
    private String mPassword;

    public ChangePwdFromAuthRequest(Context context, String hash, String password) {
        super(context);
        mHash = hash;
        mPassword = password;
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        return new JSONObject().put("hash", mHash).put("password", mPassword);
    }

    @Override
    public boolean containsAuth() {
        return true;
    }

    @Override
    public boolean isNeedAuth() {
        return false;
    }

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }
}
