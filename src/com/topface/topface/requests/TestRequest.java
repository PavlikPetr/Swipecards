package com.topface.topface.requests;

import android.content.Context;
import android.text.TextUtils;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Метод тестирования запросов к сервер и ошибок сервера
 */
@SuppressWarnings("UnusedDeclaration")
public class TestRequest extends ApiRequest {

    public static final String SERVICE_NAME = "dev.test";
    public String required;
    private String nonrequired;
    public int error;

    public TestRequest(Context context) {
        super(context);
    }

    public TestRequest(String required, String nonrequired, int error, Context context) {
        super(context);

        this.required = required;
        this.nonrequired = nonrequired;
        this.error = error;
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        JSONObject data = new JSONObject();

        data.put("required", required != null ? required : "value");

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
