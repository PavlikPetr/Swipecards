package com.topface.topface.ui.fragments.buy;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.topface.topface.App;

import org.onepf.oms.appstore.googleUtils.Purchase;

import static com.topface.topface.ui.fragments.buy.PurchasesConstants.ARG_TAG_SOURCE;


public class TransparentMarketFragment extends GoogleMarketBuyingFragment {

    public static TransparentMarketFragment newInstance(String skuId, boolean isSubscription, String from) {
        return  new TransparentMarketFragment();
    }

    @Override
    public void onPurchased(Purchase product) {
        super.onPurchased(product);
    }

    @Override
    public void onInAppBillingSupported() {

    }

    @Override
    public void onInAppBillingUnsupported() {

    }

    public void setOnPurchaseCompleteAction(onPurchaseActions purchaseCompliteAction) {
    }

    public interface onPurchaseActions {

        void onPurchaseSuccess();

        void onPopupClosed();
    }

}
