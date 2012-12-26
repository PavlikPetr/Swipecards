package com.topface.topface.requests;

import android.content.Context;
import org.json.JSONException;
import org.json.JSONObject;

public class VerifyRequest extends AbstractApiRequest {
    // Data
    public static final String service = "verify";
    public String data; // строка данных заказа от Google Play
    public String signature; // подпись данных заказа

    public VerifyRequest(Context context) {
        super(context);
        doNeedAlert(false);
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        return new JSONObject()
                .put("data", data)
                .put("signature", signature);
    }

    @Override
    public String getServiceName() {
        return service;
    }
}
