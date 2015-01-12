package com.topface.topface.requests;

import android.content.Context;
import android.text.TextUtils;

import com.topface.billing.DeveloperPayload;
import com.topface.framework.JsonUtils;
import com.topface.topface.App;
import com.topface.topface.requests.handlers.ErrorCodes;

import org.json.JSONException;
import org.json.JSONObject;
import org.onepf.oms.OpenIabHelper;
import org.onepf.oms.appstore.googleUtils.Purchase;

/**
 *
 */
abstract public class PurchaseRequest extends ApiRequest {
    transient private final Purchase mPurchase;
    transient private DeveloperPayload payload;
    private JSONObject requestData;

    protected PurchaseRequest(Purchase purchase, Context context) {
        super(context);
        doNeedAlert(false);
        mPurchase = purchase;
        this.payload = parseDeveloperPayload(purchase);
        requestData = new JSONObject();
    }

    private DeveloperPayload parseDeveloperPayload(Purchase product) {
        return JsonUtils.fromJson(product.getDeveloperPayload(), DeveloperPayload.class);
    }

    public DeveloperPayload getDeveloperPayload() {
        return payload;
    }

    public static PurchaseRequest getValidateRequest(Purchase purchase, Context context) {
        switch (purchase.getAppstoreName()) {
            case OpenIabHelper.NAME_GOOGLE:
                return new GooglePlayPurchaseRequest(purchase, context);
            case OpenIabHelper.NAME_AMAZON:
                return new AmazonPurchaseRequest(purchase, context);
            case OpenIabHelper.NAME_FORTUMO:
                return new FortumoPurchaseRequest(purchase, context);
            default:
                throw new RuntimeException("Unknown purchase app store");
        }
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        requestData.put("appStartLabel", App.getStartLabel());
        return requestData;
    }

    protected abstract String getAppstoreName();

    protected Purchase getPurchase() {
        return mPurchase;
    }

    @Override
    public void exec() {
        if (TextUtils.equals(mPurchase.getAppstoreName(), getAppstoreName())) {
            super.exec();
        } else {
            handleFail(ErrorCodes.MISSING_REQUIRE_PARAMETER, "Purchase with wrong app store");
        }
    }
}
