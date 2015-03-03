package com.topface.topface.requests;

import android.content.Context;

import com.topface.topface.utils.ClientUtils;
import com.topface.topface.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

public class RegisterRequest extends PrimalAuthRequest {
    public static final String SERVICE_NAME = "register.createAccount";

    private String mLogin;
    private String mPassword;
    private String mName;
    private long mBirthday;
    private int mSex;
    private String mLocale;

    public RegisterRequest(Context context, String login,
                           String password,
                           String name,
                           long birthday,
                           int sex) {
        super(context);
        this.mLogin = login;
        this.mPassword = password;
        this.mName = name;
        this.mBirthday = birthday;
        this.mSex = sex;
        this.mLocale = getClientLocale();
    }

    @Override
    protected String getClientLocale() {
        return ClientUtils.getClientLocale(context);
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        JSONObject data = super.getRequestData();
        data.put("login", mLogin)
                .put("password", mPassword)
                .put("name", mName)
                .put("birthday", mBirthday)
                .put("sex", mSex)
                .put("locale", mLocale);
        return data;
    }

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }

    @Override
    public boolean isNeedAuth() {
        return false;
    }
}
