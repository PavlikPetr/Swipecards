package com.topface.topface.requests;

import android.content.Context;
import android.text.TextUtils;

import com.topface.billing.DeveloperPayload;
import com.topface.framework.JsonUtils;
import com.topface.topface.requests.handlers.ErrorCodes;

import org.onepf.oms.OpenIabHelper;
import org.onepf.oms.appstore.googleUtils.Purchase;

/**
 *
 */
abstract public class PurchaseRequest extends ApiRequest {
    transient private final Purchase mPurchase;
    transient private DeveloperPayload payload;

    protected PurchaseRequest(Purchase purchase, Context context) {
        super(context);
        doNeedAlert(false);
        mPurchase = purchase;
        this.payload = parseDeveloperPayload(purchase);
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
            default:
                throw new RuntimeException("Unknown purchase app store");
        }
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
