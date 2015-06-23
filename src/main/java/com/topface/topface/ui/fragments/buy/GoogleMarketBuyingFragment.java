package com.topface.topface.ui.fragments.buy;

import com.topface.billing.OpenIabFragment;
import com.topface.topface.data.Products;
import com.topface.topface.utils.CacheProfile;

import org.onepf.oms.appstore.googleUtils.Purchase;

public abstract class GoogleMarketBuyingFragment extends OpenIabFragment {

    @Override
    public void onSubscriptionSupported() {
    }


    @Override
    public void onSubscriptionUnsupported() {
    }

    @Override
    protected Products getProducts() {
        return CacheProfile.getMarketProducts();
    }

    @Override
    public void onPurchased(Purchase product) {
        super.onPurchased(product);
    }
}
