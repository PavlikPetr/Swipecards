package com.topface.topface.requests;

import android.content.Context;
import android.text.TextUtils;

import com.topface.billing.DeveloperPayload;
import com.topface.framework.JsonUtils;
import com.topface.topface.App;
import com.topface.topface.data.AppsFlyerData;
import com.topface.topface.data.ProductsDetails;
import com.topface.topface.requests.handlers.ErrorCodes;
import com.topface.topface.utils.CacheProfile;

import org.jetbrains.annotations.Nullable;
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

    protected PurchaseRequest(Purchase purchase, Context context) {
        super(context);
        doNeedAlert(false);
        mPurchase = purchase;
        payload = getDeveloperPayload(purchase);
    }

    public DeveloperPayload getDeveloperPayload() {
        return payload;
    }

    @Nullable
    public static PurchaseRequest getValidateRequest(Purchase purchase, Context context) {
        String appstoreName = purchase.getAppstoreName();
        if (appstoreName != null) {
            switch (appstoreName) {
                case OpenIabHelper.NAME_GOOGLE:
                    return new GooglePlayPurchaseRequest(purchase, context);
                case OpenIabHelper.NAME_AMAZON:
                    return new AmazonPurchaseRequest(purchase, context);
                default:
                    throw new RuntimeException("Unknown purchase app store");
            }
        } else {
            return null;
        }
    }

    public static DeveloperPayload getDeveloperPayload(Purchase product) {
        return JsonUtils.fromJson(product.getDeveloperPayload(), DeveloperPayload.class);
    }

    @Nullable
    public static ProductsDetails.ProductDetail getProductDetail(Purchase product) {
        DeveloperPayload developerPayload = getDeveloperPayload(product);
        ProductsDetails productDetails = CacheProfile.getMarketProductsDetails();
        return productDetails != null ?
                developerPayload != null && !TextUtils.equals(developerPayload.sku, product.getSku()) ?
                        productDetails.getProductDetail(getTestProductId(product)) :
                        productDetails.getProductDetail(product.getSku())
                : null;
    }

    public static String getTestProductId(Purchase product) {
        DeveloperPayload developerPayload = getDeveloperPayload(product);
        return developerPayload != null && !TextUtils.equals(developerPayload.sku, product.getSku()) ? developerPayload.sku : null;
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        JSONObject requestData = new JSONObject();
        requestData.put("appStartLabel", App.getStartLabel());
        requestData.put("appsflyer", new AppsFlyerData(context).toJson());
        if (getDeveloperPayload() != null) {
            requestData.put("source", getDeveloperPayload().source);
        }
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
