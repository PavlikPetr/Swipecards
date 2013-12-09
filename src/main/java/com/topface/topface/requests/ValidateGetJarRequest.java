package com.topface.topface.requests;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

public class ValidateGetJarRequest extends ApiRequest {
    private static final String SERVICE = "getjar.charge";

    private String signedData;
    private String signature;
    private String transactionId;

    public ValidateGetJarRequest(Context context, String signedData, String signature, String transactionId) {
        super(context);
        this.signedData = signedData;
        this.signature = signature;
        this.transactionId = transactionId;
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        return (new JSONObject())
                .put("signedData", signedData)
                .put("signature", signature)
                .put("transaction", transactionId);
    }

    @Override
    public String getServiceName() {
        return SERVICE;
    }
}
