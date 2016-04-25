package com.topface.topface.utils;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.facebook.appevents.AppEventsConstants;
import com.facebook.appevents.AppEventsLogger;
import com.topface.billing.OpenIabFragment;
import com.topface.topface.App;
import com.topface.topface.data.BuyButtonData;
import com.topface.topface.data.Products;
import com.topface.topface.data.ProductsDetails;
import com.topface.topface.requests.PurchaseRequest;
import com.topface.topface.utils.social.AuthToken;
import com.topface.topface.utils.social.FbAuthorizer;

import org.onepf.oms.appstore.googleUtils.Purchase;

import java.util.LinkedList;

/**
 * Created by ppavlik on 15.04.16.
 * Different methods for purchases
 */
public class PurchasesUtils {

    @Nullable
    public static BuyButtonData getButtonBySku(String sku) {
        Products products = CacheProfile.getMarketProducts();
        LinkedList<BuyButtonData> arrayButtons = new LinkedList<>();
        arrayButtons.addAll(products.likes);
        arrayButtons.addAll(products.coins);
        arrayButtons.addAll(products.premium);
        arrayButtons.addAll(products.others);
        arrayButtons.addAll(products.coinsSubscriptions);
        arrayButtons.addAll(products.coinsSubscriptionsMasked);
        for (BuyButtonData data : arrayButtons) {
            if (data.id.equals(sku)) {
                return data;
            }
        }
        return null;
    }

    public static boolean isTrial(Purchase product) {
        String originalSku = PurchaseRequest.getDeveloperPayload(product).sku;
        BuyButtonData button = getButtonBySku(originalSku);
        return button != null && button.trialPeriodInDays > 0;
    }

    public static boolean isTestPurchase(Purchase product) {
        return product.getSku().equals(OpenIabFragment.TEST_PURCHASED_PRODUCT_ID);
    }

    public static void sendPurchaseEvent(int productsCount, String productType, String productId, String currencyCode, double price, String transactionId, boolean isTrial, boolean isTestPurchase) {
        // если покупка триальная или тестовая, то во Flurry уйдет событие со стоимостью "0"
        FlurryManager.getInstance().sendPurchaseEvent(productId, isTrial || isTestPurchase ? 0 : price, currencyCode);
        if (!isTestPurchase && !isTrial) {
            PurchasesEvents.purchaseSuccess(productsCount, productType, productId, currencyCode, price, transactionId);
        }
        if (AuthToken.getInstance().getSocialNet().equals(AuthToken.SN_FACEBOOK) && !isTestPurchase && !isTrial) {
            FbAuthorizer.initFB();
            AppEventsLogger logger = AppEventsLogger.newLogger(App.getContext());
            Bundle bundle = new Bundle();
            bundle.putString(AppEventsConstants.EVENT_PARAM_NUM_ITEMS, String.valueOf(productsCount));
            bundle.putString(AppEventsConstants.EVENT_PARAM_CONTENT_TYPE, productType);
            bundle.putString(AppEventsConstants.EVENT_PARAM_CONTENT_ID, productId);
            bundle.putString(AppEventsConstants.EVENT_PARAM_CURRENCY, currencyCode);
            logger.logEvent(AppEventsConstants.EVENT_NAME_PURCHASED, price, bundle);
        }
    }

    public static void sendPurchaseEvent(Purchase product) {
        String originalSku = PurchaseRequest.getDeveloperPayload(product).sku;
        BuyButtonData button = PurchasesUtils.getButtonBySku(originalSku);
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
