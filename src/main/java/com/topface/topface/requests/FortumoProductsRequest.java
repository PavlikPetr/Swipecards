package com.topface.topface.requests;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Запрос списка продуктов для вкладки fortumo (SMS) на экране покупок
 */
public class FortumoProductsRequest extends ApiRequest {
    private static final String SERVICE_NAME = "fortumo.getProducts";

    public FortumoProductsRequest(Context context) {
        super(context);
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        return null;
    }

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }
}
