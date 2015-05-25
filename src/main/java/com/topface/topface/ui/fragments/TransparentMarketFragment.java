package com.topface.topface.ui.fragments;

import com.topface.topface.App;
import com.topface.topface.data.experiments.SixCoinsSubscribeExperiment;
import com.topface.topface.ui.fragments.buy.MarketBuyingFragment;
import com.topface.topface.utils.CacheProfile;

import org.onepf.oms.appstore.googleUtils.Purchase;


public class TransparentMarketFragment extends MarketBuyingFragment {

    private onPurchaseCompliteAction mPurchaseCompliteAction;

    public void onOpenIabSetupFinished(boolean normaly) {
        super.onOpenIabSetupFinished(normaly);
        SixCoinsSubscribeExperiment experiment = CacheProfile.getOptions().sixCoinsSubscribeExperiment;
        if (CacheProfile.isEmpty()) {
            setTestPaymentsState(App.getUserConfig().getTestPaymentFlag());
        }
        buyNow(experiment.productId, experiment.subscription);
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
        mPurchaseCompliteAction.onPurchaseAction();
    }

    public void setOnPurchaseCompliteAction(onPurchaseCompliteAction purchaseCompliteAction) {
        this.mPurchaseCompliteAction = purchaseCompliteAction;
    }

    public interface onPurchaseCompliteAction {
        void onPurchaseAction();
    }
}
