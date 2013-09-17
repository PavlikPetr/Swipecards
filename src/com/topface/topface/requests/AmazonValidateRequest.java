package com.topface.topface.requests;

import android.content.Context;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Метод проверки платежа от Amazon
 */
public class AmazonValidateRequest extends ApiRequest {

    private String mUserId;

    private String mPurchaseToken;

    public static final String SERVICE_NAME = "amazon.purchase";
    private String mRequestId;
    private final String mSku;

    public AmazonValidateRequest(String sku, String userId, String purchaseToken, String requestId, Context context) {
        super(context);
        mSku = sku;
        mUserId = userId;
        mPurchaseToken = purchaseToken;
        mRequestId = requestId;
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        return new JSONObject()
                .put("product", mSku)
                .put("userId", mUserId)
                .put("requestId", mRequestId)
                .put("token", mPurchaseToken);
    }

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }
}
