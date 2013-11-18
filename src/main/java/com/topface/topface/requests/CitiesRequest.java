package com.topface.topface.requests;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

public class CitiesRequest extends ApiRequest {
    // Data
    public static final String SERVICE_NAME = "geo.getCities";
    public String type; // тип выборки перечня городов. Пока поддерживается только “top”
    public String prefix;

    public CitiesRequest(Context context) {
        super(context);
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        JSONObject data = new JSONObject();
        if (type != null) {
            data.put("type", type);
        }
        if(prefix != null) {
            data.put("prefix", prefix);
        }
        return data;
    }

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }
}
