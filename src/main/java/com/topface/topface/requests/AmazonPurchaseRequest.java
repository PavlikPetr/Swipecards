package com.topface.topface.requests;

import android.content.Context;

import com.topface.framework.JsonUtils;

import org.json.JSONException;
import org.json.JSONObject;
import org.onepf.oms.OpenIabHelper;
import org.onepf.oms.appstore.googleUtils.Purchase;

/**
 * Метод проверки платежа от Amazon
 */
public class AmazonPurchaseRequest extends PurchaseRequest {
    private static final String SERVICE_NAME = "amazon.purchase";

    public AmazonPurchaseRequest(Purchase purchase, Context context) {
        super(purchase, context);
    }

    @Override
    protected String getAppstoreName() {
        return OpenIabHelper.NAME_AMAZON;
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        Purchase purchase = getPurchase();
        AmazonPurchaseResponse amazonResponse = JsonUtils.fromJson(
                purchase.getOriginalJson(),
                AmazonPurchaseResponse.class
        );
        return new JSONObject()
                .put("product", purchase.getSku())
                .put("userId", amazonResponse.userId)
                .put("requestId", purchase.getOrderId())
                .put("token", purchase.getToken());
    }

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }

    @SuppressWarnings("UnusedDeclaration")
    private class AmazonPurchaseResponse {
        String orderId;
        String productId;
        String purchaseStatus;
        String userId;
        String itemType;
        String purchaseToken;
    }
}