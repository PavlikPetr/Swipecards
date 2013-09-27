package com.topface.billing.amazon;

import android.app.Activity;

import com.amazon.inapp.purchasing.PurchasingManager;
import com.topface.billing.BillingDriver;
import com.topface.billing.BillingListener;

/**
 * Платежный драйвер для Amazon In-App Purchasing
 * <p/>
 * NOTE: В AmazonBillingDriver не поддерживаются события onInAppBillingUnsupported и onSubscriptionBillingUnsupported
 * Будут выполнены только коллбэки, когда API доступно
 */
public class AmazonBillingDriver extends BillingDriver {

    public AmazonBillingDriver(Activity activity, BillingListener listener) {
        super(activity, listener);
    }

    @Override
    public void onStart() {
        AmazonPurchaseObserver observer = new AmazonPurchaseObserver(getActivity(), this);
        PurchasingManager.registerObserver(observer);
    }

    @Override
    public void onResume() {
        PurchasingManager.initiateGetUserIdRequest();
    }

    @Override
    public void onStop() {
    }

    @Override
    public void onDestroy() {
    }

    @Override
    public void buyItem(String itemId) {
        PurchasingManager.initiatePurchaseRequest(itemId);
    }

    @Override
    public void buySubscription(String subscriptionId) {
        PurchasingManager.initiatePurchaseRequest(subscriptionId);
    }


}
