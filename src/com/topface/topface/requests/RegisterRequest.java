package com.topface.topface.requests;

import android.content.Context;
import com.topface.topface.utils.ClientUtils;
import com.topface.topface.utils.Utils;
import org.json.JSONException;
import org.json.JSONObject;

public class RegisterRequest extends ApiRequest {
    public static final String SERVICE_NAME = "register";

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
        this.clientType = Utils.getBuildType();
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
                .put("clienttype", clientType)
                .put("locale", locale);
    }

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }
}
