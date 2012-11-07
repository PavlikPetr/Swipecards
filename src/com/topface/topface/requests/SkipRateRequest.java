package com.topface.topface.requests;

import android.content.Context;
import org.json.JSONException;
import org.json.JSONObject;

public class SkipRateRequest extends AbstractApiRequest {
    public static final String SERVICE_NAME = "skipRate";
    public int userid; // идентификатор пользователя для оценки

    public SkipRateRequest(Context context) {
        super(context);
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        return new JSONObject()
                .put("userid", userid);
    }

    @Override
    protected String getServiceName() {
        return SERVICE_NAME;
    }
}
