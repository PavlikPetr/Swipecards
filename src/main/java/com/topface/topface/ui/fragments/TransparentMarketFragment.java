package com.topface.topface.ui.fragments;

import android.support.v4.app.Fragment;

import com.topface.topface.App;
import com.topface.topface.data.experiments.SixCoinsSubscribeExperiment;
import com.topface.topface.ui.fragments.buy.MarketBuyingFragment;
import com.topface.topface.ui.fragments.feed.LikesFragment;
import com.topface.topface.utils.CacheProfile;

import org.onepf.oms.appstore.googleUtils.Purchase;


public class TransparentMarketFragment extends MarketBuyingFragment {

    public void onOpenIabSetupFinished(boolean normaly) {
        super.onOpenIabSetupFinished(normaly);
        SixCoinsSubscribeExperiment experiment = CacheProfile.getOptions().sixCoinsSubscribeExperiment;
        setTestPaymentsState(App.getUserConfig().getTestPaymentFlag());
        buyNow(getCoinsProducts(getProducts(), false).get(0).id, experiment.subscription);
    }

    public void buyNow(String id, boolean isSubscription) {
        if (id != null) {
            if (isSubscription && !isTestPurchasesEnabled()) {
                buySubscription(id);
            } else {
                buyItem(id);
            }
        }
    }

    @Override
    public void onPurchased(Purchase product) {
        super.onPurchased(product);
        Fragment fragment = getParentFragment();
        if (fragment.isAdded() && fragment instanceof LikesFragment) {
            ((LikesFragment) fragment).updateData(false, true);
        }
    }
}
