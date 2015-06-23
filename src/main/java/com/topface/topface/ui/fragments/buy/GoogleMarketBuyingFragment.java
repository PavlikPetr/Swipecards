package com.topface.topface.ui.fragments.buy;

import com.topface.billing.OpenIabFragment;

import org.onepf.oms.appstore.googleUtils.Purchase;

public abstract class GoogleMarketBuyingFragment extends OpenIabFragment {

    @Override
    public void onSubscriptionSupported() {
    }


    @Override
    public void onSubscriptionUnsupported() {
    }

    @Override
    public void onPurchased(Purchase product) {
        super.onPurchased(product);
    }
}
