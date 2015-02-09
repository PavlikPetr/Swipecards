package com.topface.topface.requests;

import android.content.Context;

import com.topface.topface.BuildConfig;
import com.topface.topface.utils.ClientUtils;
import com.topface.topface.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

public class RegisterRequest extends ApiRequest {
    public static final String SERVICE_NAME = "register.createAccount";

    private String login;
    private String password;
    private String name;
    private long birthday;
    private int sex;
    private String clientType;
    private String locale;

    public RegisterRequest(Context context, String login,
                           String password,
                           String name,
                           long birthday,
                           int sex) {
        super(context);
        this.login = login;
        this.password = password;
        this.name = name;
        this.birthday = birthday;
        this.sex = sex;
        this.clientType = BuildConfig.BILLING_TYPE.getClientType();
        this.locale = ClientUtils.getClientLocale(context);
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        return new JSONObject()
                .put("login", login)
                .put("password", password)
                .put("name", name)
                .put("birthday", birthday)
                .put("sex", sex)
                .put("clientType", clientType)
                .put("locale", locale)
                .put("clientCarrier", Utils.getCarrierName());
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
