package com.topface.topface.requests;

import android.content.Context;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Реализация метода api options
 */
public class UserGetAppOptionsRequest extends ApiRequest {

    public static final String SERVICE = "user.getAppOptions";
    public static final String VERSION_INTENT = "com.topface.topface.OPTIONS";

    public UserGetAppOptionsRequest(Context context) {
        super(context);
        doNeedAlert(false);
    }

    @Override
    public String getServiceName() {
        return SERVICE;
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        return null;
    }
}
