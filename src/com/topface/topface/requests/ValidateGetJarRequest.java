package com.topface.topface.requests;

import android.content.Context;
import org.json.JSONException;
import org.json.JSONObject;

public class ValidateGetJarRequest extends ApiRequest{

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
                .put("transactionId", transactionId);
    }

    @Override
    public String getServiceName() {
        return "validateGetjar";
    }
}
