package com.topface.topface.requests;

import android.content.Context;
import com.google.analytics.tracking.android.EasyTracker;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class UserRequest extends ApiRequest {
    private static final String SERVICE = "user.getProfile";

    private int userId; // массив id пользователя в топфейсе
    public Boolean visitor = true; // флаг определения текущего пользователя посетителем профилей запрошенных пользователей

    public UserRequest(int uid, Context context) {
        super(context);
        userId = uid;
        doNeedAlert(false);

    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        return new JSONObject()
                .put("userId", userId)
                .put("visitor", visitor);
    }

    @Override
    public String getServiceName() {
        return SERVICE;
    }

    @Override
    public void exec() {
        super.exec();
        EasyTracker.getTracker().sendEvent("Profile", "LoadUser", "", 1l);
    }
}
