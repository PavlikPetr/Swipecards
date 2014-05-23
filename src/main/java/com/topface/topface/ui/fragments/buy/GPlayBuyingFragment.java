package com.topface.topface.ui.fragments.buy;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.topface.billing.BillingFragment;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.data.Products;
import com.topface.topface.data.Products.ProductsInfo.CoinsSubscriptionInfo;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.handlers.SimpleApiHandler;
import com.topface.topface.ui.ContainerActivity;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.CountersManager;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.offerwalls.OfferwallsManager;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

import static com.topface.topface.data.Products.BuyButton;
import static com.topface.topface.data.Products.BuyButtonClickListener;

public class GPlayBuyingFragment extends AbstractBuyingFragment {


    private Products.BuyButtonClickListener mCoinsSubscriptionClickListener = new Products.BuyButtonClickListener() {
        @Override
        public void onClick(String id) {
            startActivityForResult(ContainerActivity.getCoinsSubscriptionIntent(getFrom()), ContainerActivity.INTENT_COINS_SUBSCRIPTION_FRAGMENT);
        }
    };

    protected LinkedList<BuyButton> getCoinsProducts(@NotNull Products products, boolean coinsMaskedExperiment) {
        boolean hasMaskedCoinsSubs = products.info != null
                && products.info.coinsSubscriptionMasked != null
                && products.info.coinsSubscriptionMasked.status != null
                && products.info.coinsSubscriptionMasked.status.isActive();
        return coinsMaskedExperiment && !hasMaskedCoinsSubs ? products.coinsSubscriptionsMasked : products.coins;
    }

    @Override
    public Products getProducts() {
        return CacheProfile.getProducts();
    }

    @Override
    public BuyButtonClickListener getCoinsSubscriptionClickListener() {
        return null;
    }

    protected View getCoinsSubscriptionsButton(Products products, LinearLayout coinsButtons) {
        if (!products.coinsSubscriptions.isEmpty()) {
            CoinsSubscriptionInfo info = products.info.coinsSubscription;
            BuyButton btn = info.status.isActive() ? info.hasSubscriptionButton : info.noSubscriptionButton;
            return Products.setOpenButton(coinsButtons, btn,
                    getActivity(), mCoinsSubscriptionClickListener);
        }
        return null;
    }




    @Override
    protected String getTitle() {
        return getString(R.string.buying_header_title);
    }
}
