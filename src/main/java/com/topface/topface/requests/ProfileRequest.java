package com.topface.topface.requests;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

public class ProfileRequest extends ApiRequest {
    public static final String SERVICE = "user.getOwnProfile";

    public ProfileRequest(Context context) {
        super(context);
        doNeedAlert(false); //чтобы не предупреждать пользователя алертом о пропаже инета
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        return null;
    }

    @Override
    public String getServiceName() {
        return SERVICE;
    }

}
