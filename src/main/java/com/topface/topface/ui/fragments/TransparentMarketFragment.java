package com.topface.topface.ui.fragments;

import com.topface.topface.App;
import com.topface.topface.data.experiments.SixCoinsSubscribeExperiment;
import com.topface.topface.ui.fragments.buy.MarketBuyingFragment;
import com.topface.topface.utils.CacheProfile;

import org.onepf.oms.appstore.googleUtils.Purchase;


public class TransparentMarketFragment extends MarketBuyingFragment {

    private onPurchaseCompleteAction mPurchaseCompleteAction;

    public void onOpenIabSetupFinished(boolean normaly) {
        super.onOpenIabSetupFinished(normaly);
        SixCoinsSubscribeExperiment experiment = CacheProfile.getOptions().sixCoinsSubscribeExperiment;
        if (isTestPurchasesAvailable()) {
            setTestPaymentsState(App.getUserConfig().getTestPaymentFlag());
        }
        buyNow(experiment.productId, experiment.isSubscription);
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
        mPurchaseCompleteAction.onPurchaseAction();
    }

    @Override
    public void onResume() {
        super.onResume();
        //Устанавливаем тестовые покупки
        if (isTestPurchasesAvailable()) {
            setTestPaymentsState(App.getUserConfig().getTestPaymentFlag());
        }
    }

    public void setOnPurchaseCompleteAction(onPurchaseCompleteAction purchaseCompliteAction) {
        this.mPurchaseCompleteAction = purchaseCompliteAction;
    }

    public interface onPurchaseCompleteAction {
        void onPurchaseAction();
    }
}
