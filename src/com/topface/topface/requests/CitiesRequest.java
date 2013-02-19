package com.topface.topface.requests;

import android.content.Context;
import org.json.JSONException;
import org.json.JSONObject;

public class CitiesRequest extends ApiRequest {
    // Data
    public static final String SERVICE_NAME = "cities";
    public String type; // тип выборки перечня городов. Пока поддерживается только “top”

    public CitiesRequest(Context context) {
        super(context);
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        return new JSONObject().put("type", type);
    }

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }
}
