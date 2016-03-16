package com.topface.topface.requests;

import android.content.Context;
import android.text.TextUtils;

import com.topface.topface.App;
import com.topface.topface.data.AppsFlyerData;
import com.topface.topface.data.ProductsDetails;

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
    private float cost = 0;// {Number} cost - стоимость покупки в валюте пользователя
    private String currencyCode; // {String} currencyCode - код валюты пользователя ISO 4217

    public GooglePlayPurchaseRequest(Purchase product, Context context) {
        super(product, context);

        this.data = product.getOriginalJson();
        this.signature = product.getSignature();
        this.testProductId = PurchaseRequest.getTestProductId(product);
        ProductsDetails.ProductDetail detail = PurchaseRequest.getProductDetail(product);
        if (detail != null) {
            currencyCode = detail.currency;
            cost = (float) (detail.price / ProductsDetails.MICRO_AMOUNT);
        }
    }

    @Override
    protected String getAppstoreName() {
        return OpenIabHelper.NAME_GOOGLE;
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        JSONObject requestData = super.getRequestData();
        requestData
                .put("data", data)
                .put("signature", signature)
                .put("source", getDeveloperPayload().source);
        requestData.put("appsflyer", new AppsFlyerData(context).toJsonWithConversions(App.getConversionHolder()));
        //Если включены тестовые платежи, то отправляем еще и id оригинального платежа,
        //что бы нам начислил реальный продукт
        if (!TextUtils.isEmpty(testProductId)) {
            requestData.put("testProductId", testProductId);
        }
        if (!TextUtils.isEmpty(currencyCode)) {
            requestData.put("currencyCode", currencyCode)
                    .put("cost", cost);
        }
        return requestData;
    }

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }


}
