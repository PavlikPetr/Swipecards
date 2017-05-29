package com.topface.topface.utils;

import android.os.Bundle;

import com.facebook.appevents.AppEventsConstants;
import com.topface.billing.OpenIabFragment;
import com.topface.topface.data.BuyButtonData;
import com.topface.topface.data.ProductsDetails;
import com.topface.topface.requests.PurchaseRequest;
import com.topface.topface.statistics.FBStatistics;
import com.topface.topface.utils.extensions.ProductExtensionKt;

import org.onepf.oms.appstore.googleUtils.Purchase;

/**
 * Created by ppavlik on 15.04.16.
 * Different methods for purchases
 */
public class PurchasesUtils {

    public static boolean isTrial(Purchase product) {
        String originalSku = PurchaseRequest.getDeveloperPayload(product).sku;
        BuyButtonData button = ProductExtensionKt.getProduct(originalSku);
        return button != null && button.trialPeriodInDays > 0;
    }

    public static boolean isTestPurchase(Purchase product) {
        return product.getSku().equals(OpenIabFragment.TEST_PURCHASED_PRODUCT_ID);
    }

    public static void sendPurchaseEvent(int productsCount, String productType, String productId, String currencyCode, double price, String transactionId, boolean isTrial, boolean isTestPurchase) {
        // если покупка триальная или тестовая, то во Flurry уйдет событие со стоимостью "0"
        FlurryManager.getInstance().sendPurchaseEvent(productId, isTrial || isTestPurchase ? 0 : price, currencyCode);
        if (!isTestPurchase && !isTrial) {
            new PurchasesEvents().purchaseSuccess(productsCount, productType, productId, currencyCode, price, transactionId);
        }
        if (!isTestPurchase && !isTrial) {
            Bundle bundle = new Bundle();
            bundle.putString(AppEventsConstants.EVENT_PARAM_NUM_ITEMS, String.valueOf(productsCount));
            bundle.putString(AppEventsConstants.EVENT_PARAM_CONTENT_TYPE, productType);
            bundle.putString(AppEventsConstants.EVENT_PARAM_CONTENT_ID, productId);
            FBStatistics.INSTANCE.onPurchase(price, currencyCode, bundle);
        }
    }

    public static void sendPurchaseEvent(Purchase product) {
        String originalSku = PurchaseRequest.getDeveloperPayload(product).sku;
        BuyButtonData button = ProductExtensionKt.getProduct(originalSku);
        ProductsDetails.ProductDetail detail = PurchaseRequest.getProductDetail(product);
        if (detail != null) {
            PurchasesUtils.sendPurchaseEvent(1,
                    button != null ? button.type.getName() : Utils.EMPTY,
                    originalSku,
                    detail.currency,
                    detail.price / ProductsDetails.MICRO_AMOUNT,
                    product.getOrderId(),
                    button != null && button.trialPeriodInDays > 0,
                    PurchasesUtils.isTestPurchase(product));
        }
    }
}
