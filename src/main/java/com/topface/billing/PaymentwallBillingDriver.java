package com.topface.billing;

import android.app.Activity;
import android.text.TextUtils;

import com.topface.topface.utils.CacheProfile;

/**
 * Патежный драйвер который ничего не делает, используется для типов сборки, где нет платежей через маркет, а только Paymentwall
 */
public class PaymentwallBillingDriver extends BillingDriver {

    public PaymentwallBillingDriver(Activity activity, BillingListener listener) {
        super(activity, listener);
    }

    @Override
    public void onStart() {
        BillingSupportListener billingSupportListener = getBillingSupportListener();
        if (billingSupportListener != null) {
            billingSupportListener.onInAppBillingUnsupported();
        }
    }

    @Override
    public void onResume() {

    }

    @Override
    public void onStop() {

    }

    @Override
    public void onDestroy() {

    }

    @Override
    public void buyItem(String itemId) {

    }

    @Override
    public void buySubscription(String subscriptionId) {

    }

    @Override
    public boolean isTestPurchasesAvailable() {
        return false;
    }
}
