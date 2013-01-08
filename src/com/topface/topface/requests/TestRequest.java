package com.topface.topface.requests;

import android.content.Context;
import android.text.TextUtils;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Метод тестирования запросов к сервер и ошибок сервера
 */
@SuppressWarnings("UnusedDeclaration")
public class TestRequest extends AbstractApiRequest {

    public static final String SERVICE_NAME = "test";
    public String required;
    private String nonrequired;
    private int error;

    public TestRequest(Context context) {
        super(context);
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        JSONObject data = new JSONObject();
        if (!TextUtils.isEmpty(required)) {
            data.put("required", required);
        }
        if (!TextUtils.isEmpty(nonrequired)) {
            data.put("nonrequired", nonrequired);
        }
        if (error > 0) {
            data.put("error", error);
        }
        return data;
    }

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }
}
