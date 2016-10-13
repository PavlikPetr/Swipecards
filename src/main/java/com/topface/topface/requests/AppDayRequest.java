package com.topface.topface.requests;

import android.content.Context;
import android.os.StrictMode;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Класс запроса приложения дня
 * Created by siberia87 on 10.10.16.
 */

public class AppDayRequest extends ApiRequest {
    private static String SERVICE_NAME = "ad.appListOfTheDay";
    private String mTypeFeedFragment;

    public AppDayRequest(Context context, String typeFeedFragment) {
        super(context);
        mTypeFeedFragment = typeFeedFragment;

    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        return new JSONObject().put("type", mTypeFeedFragment);
    }

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }
}
