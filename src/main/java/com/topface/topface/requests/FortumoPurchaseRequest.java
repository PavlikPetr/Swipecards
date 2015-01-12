package com.topface.topface.requests;

import android.content.Context;

import com.topface.billing.DeveloperPayload;
import com.topface.framework.JsonUtils;

import org.json.JSONException;
import org.json.JSONObject;
import org.onepf.oms.OpenIabHelper;
import org.onepf.oms.appstore.googleUtils.Purchase;

/**
 * Request for verifing fortumo purchases.
 */
public class FortumoPurchaseRequest extends PurchaseRequest {

    public static final String SERVICE_NAME = "fortumo.purchase";

    private String source;
    private String orderId;

    protected FortumoPurchaseRequest(Purchase purchase, Context context) {
        super(purchase, context);
        DeveloperPayload payload = JsonUtils.fromJson(purchase.getDeveloperPayload(), DeveloperPayload.class);
        if (payload != null) {
            source = payload.source;
        } else {
            source = "";
        }
        orderId = purchase.getOrderId();
    }

    @Override
    protected String getAppstoreName() {
        return OpenIabHelper.NAME_FORTUMO;
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        return super.getRequestData()
                .put("source", source)
                .put("orderId", orderId);
    }

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }
}
