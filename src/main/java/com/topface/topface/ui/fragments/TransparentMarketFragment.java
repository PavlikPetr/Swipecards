package com.topface.topface.ui.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.topface.topface.App;
import com.topface.topface.ui.fragments.buy.MarketBuyingFragment;

import org.onepf.oms.appstore.googleUtils.Purchase;


public class TransparentMarketFragment extends MarketBuyingFragment {

    public final static String SUBSCRIPTION_ID = "subscription_id";
    public final static String IS_SUBSCRIPTION = "is_subscription";

    private onPurchaseCompleteAction mPurchaseCompleteAction;
    private String mSubscriptionId;
    private boolean mIsSubscription;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        if (null != bundle) {
            if (getArguments().containsKey(SUBSCRIPTION_ID)) {
                mSubscriptionId = getArguments().getString(SUBSCRIPTION_ID, "");
            }
            if (getArguments().containsKey(IS_SUBSCRIPTION)) {
                mIsSubscription = getArguments().getBoolean(IS_SUBSCRIPTION);
            }
        }
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    public void onOpenIabSetupFinished(boolean normaly) {
        super.onOpenIabSetupFinished(normaly);
        if (isTestPurchasesAvailable()) {
            setTestPaymentsState(App.getUserConfig().getTestPaymentFlag());
        }
        if (!TextUtils.isEmpty(mSubscriptionId)) {
            buyNow(mSubscriptionId, mIsSubscription);
        }
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
