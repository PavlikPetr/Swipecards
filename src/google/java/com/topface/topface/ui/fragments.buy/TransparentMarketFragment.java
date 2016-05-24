package com.topface.topface.ui.fragments.buy;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.topface.topface.App;
import com.topface.topface.ui.views.ITransparentMarketFragmentRunner;

import org.onepf.oms.appstore.googleUtils.Purchase;


public class TransparentMarketFragment extends GoogleMarketBuyingFragment implements ITransparentMarketFragmentRunner {

    public final static String PRODUCT_ID = "product_id";
    public final static String IS_SUBSCRIPTION = "is_subscription";

    private onPurchaseActions mPurchaseActions;
    private String mSubscriptionId;
    private boolean mIsSubscription;
    private boolean isNeedCloseFragment = false;

    public static TransparentMarketFragment newInstance(String skuId, boolean isSubscription) {
        final TransparentMarketFragment fragment = new TransparentMarketFragment();
        Bundle bundle = new Bundle();
        bundle.putString(TransparentMarketFragment.PRODUCT_ID, skuId);
        bundle.putBoolean(TransparentMarketFragment.IS_SUBSCRIPTION, isSubscription);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        if (null != bundle) {
            if (getArguments().containsKey(PRODUCT_ID)) {
                mSubscriptionId = getArguments().getString(PRODUCT_ID, "");
            }
            if (getArguments().containsKey(IS_SUBSCRIPTION)) {
                mIsSubscription = getArguments().getBoolean(IS_SUBSCRIPTION);
            }
        }
        super.onCreate(savedInstanceState);
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
        if (mPurchaseActions != null) {
            mPurchaseActions.onPurchaseSuccess();
        }
    }

    @Override
    public void onInAppBillingSupported() {

    }

    @Override
    public void onInAppBillingUnsupported() {

    }

    @Override
    public void onResume() {
        super.onResume();
        if (mPurchaseActions != null && isNeedCloseFragment) {
            mPurchaseActions.onPopupClosed();
        }
        //Устанавливаем тестовые покупки
        if (isTestPurchasesAvailable()) {
            setTestPaymentsState(App.getUserConfig().getTestPaymentFlag());
        }
    }

    @Override
    public void setOnPurchaseCompleteAction(onPurchaseActions purchaseCompliteAction) {
        this.mPurchaseActions = purchaseCompliteAction;
    }

    public interface onPurchaseActions {

        void onPurchaseSuccess();

        void onPopupClosed();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        isNeedCloseFragment = true;
        super.onActivityResult(requestCode, resultCode, data);
    }
}
