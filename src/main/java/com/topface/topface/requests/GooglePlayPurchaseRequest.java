package com.topface.topface.requests;

import android.content.Context;
import android.text.TextUtils;

import com.topface.billing.DeveloperPayload;
import com.topface.topface.data.AppsFlyerData;

import org.json.JSONException;
import org.json.JSONObject;
import org.onepf.oms.OpenIabHelper;
import org.onepf.oms.appstore.googleUtils.Purchase;

public class GooglePlayPurchaseRequest extends PurchaseRequest {
    // Data
    public static final String SERVICE_NAME = "googleplay.purchase";
    private String data; // строка данных заказа от Google Play
    private String signature; // подпись данных заказа
    private String testProductId; //id продукта, нужен при тестовых покупках

    public GooglePlayPurchaseRequest(Purchase product, Context context) {
        super(product, context);

        this.data = product.getOriginalJson();
        this.signature = product.getSignature();
        DeveloperPayload developerPayload = getDeveloperPayload();
        //Если SKU из DeveloperPayload не соответсвует тому, что мы купили, то это тестовая покупка
        //и нам нужно добавить соответсвующий параметр, что бы сервер нам корректно начислил продукт
        if (developerPayload != null && !TextUtils.equals(developerPayload.sku, product.getSku())) {
            this.testProductId = developerPayload.sku;
        }
    }

    @Override
    protected String getAppstoreName() {
        return OpenIabHelper.NAME_GOOGLE;
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        JSONObject requestData = new JSONObject();
        requestData
                .put("data", data)
                .put("signature", signature)
                .put("source", getDeveloperPayload().source)
                .put("appsflyer", new AppsFlyerData(context).toJson());

        //Если включены тестовые платежи, то отправляем еще и id оригинального платежа,
        //что бы нам начислил реальный продукт
        if (!TextUtils.isEmpty(testProductId)) {
            requestData.put("testProductId", testProductId);
        }

        return requestData;
    }

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }


}
