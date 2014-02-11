package com.topface.topface.requests;

import android.content.Context;
import android.text.TextUtils;

import com.topface.billing.BillingDriver;
import com.topface.topface.data.AppsFlyerData;

import org.json.JSONException;
import org.json.JSONObject;

public class GooglePlayPurchaseRequest extends ApiRequest {
    // Data
    public static final String SERVICE_NAME = "googleplay.purchase";
    public String data; // строка данных заказа от Google Play
    public String signature; // подпись данных заказа
    public AppsFlyerData appsflyer;

    public GooglePlayPurchaseRequest(Context context) {
        super(context);
        doNeedAlert(false);
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        JSONObject requestData = new JSONObject();
        requestData
                .put("data", data)
                .put("signature", signature)
                .put("source", BillingDriver.getSourceValue());
        if (appsflyer != null) {
            requestData.put("appsflyer", appsflyer.toJson());
        }

        //Если включены тестовые платежи, то отправляем еще и id оригинального платежа,
        //что бы нам начислил сервер нужную покупку
        String productIdForTestPayment = BillingDriver.getProductIdForTestPayment();
        if (!TextUtils.isEmpty(productIdForTestPayment)) {
            requestData.put("testProductId", productIdForTestPayment);
        }

        return requestData;
    }

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }
}
