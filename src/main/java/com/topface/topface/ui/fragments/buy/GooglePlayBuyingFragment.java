package com.topface.topface.ui.fragments.buy;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.LinearLayout;

import com.topface.topface.data.Products;
import com.topface.topface.data.Products.ProductsInfo.CoinsSubscriptionInfo;
import com.topface.topface.ui.CoinsSubscriptionsActivity;
import com.topface.topface.ui.fragments.PurchasesFragment;
import com.topface.topface.utils.CacheProfile;

import java.util.LinkedList;

import static com.topface.topface.data.Products.BuyButton;
import static com.topface.topface.data.Products.BuyButtonClickListener;

public class GooglePlayBuyingFragment extends CoinsBuyingFragment {

    public static GooglePlayBuyingFragment newInstance(String from) {
        GooglePlayBuyingFragment buyingFragment = new GooglePlayBuyingFragment();
        if (from != null) {
            Bundle args = new Bundle();
            args.putString(ARG_TAG_SOURCE, from);
            buyingFragment.setArguments(args);
        }
        return buyingFragment;
    }


    public static GooglePlayBuyingFragment newInstance(int type, int coins, String from) {
        GooglePlayBuyingFragment fragment = new GooglePlayBuyingFragment();
        setArguments(type, coins, from, fragment);
        return fragment;
    }

    protected static void setArguments(int type, int coins, String from, GooglePlayBuyingFragment fragment) {
        Bundle args = new Bundle();
        args.putInt(PurchasesFragment.ARG_ITEM_TYPE, type);
        args.putInt(PurchasesFragment.ARG_ITEM_PRICE, coins);
        if (from != null) {
            args.putString(ARG_TAG_SOURCE, from);
        }
        fragment.setArguments(args);
    }

    private Products.BuyButtonClickListener mCoinsSubscriptionClickListener = new Products.BuyButtonClickListener() {
        @Override
        public void onClick(String id) {
            startActivityForResult(CoinsSubscriptionsActivity.createIntent(getFrom()), CoinsSubscriptionsActivity.INTENT_COINS_SUBSCRIPTION);
        }
    };

    protected LinkedList<BuyButton> getCoinsProducts(@NonNull Products products, boolean coinsMaskedExperiment) {
        boolean hasMaskedCoinsSubs = products.info != null
                && products.info.coinsSubscriptionMasked != null
                && products.info.coinsSubscriptionMasked.status != null
                && products.info.coinsSubscriptionMasked.status.isActive();
        return coinsMaskedExperiment &&
                !hasMaskedCoinsSubs &&
                products.coinsSubscriptionsMasked.size() > 0 ?
                products.coinsSubscriptionsMasked :
                products.coins;
    }

    @Override
    public Products getProducts() {
        return CacheProfile.getMarketProducts();
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
}
