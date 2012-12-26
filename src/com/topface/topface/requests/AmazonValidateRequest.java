package com.topface.topface.requests;

import android.content.Context;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Метод проверки платежа от Amazon
 */
public class AmazonValidateRequest extends AbstractApiRequest {

    private String mUserId;

    private String mPurchaseToken;

    public static final String SERVICE_NAME = "validate";

    public AmazonValidateRequest(String userId, String purchaseToken, Context context) {
        super(context);
        mUserId = userId;
        mPurchaseToken = purchaseToken;
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        return new JSONObject()
                .put("userid", mUserId)
                .put("token", mPurchaseToken);
    }

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }
}
