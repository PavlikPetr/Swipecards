package com.topface.topface.ui.fragments.buy;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.LinearLayout;

import com.topface.topface.data.PaymentWallProducts;
import com.topface.topface.data.Products;
import com.topface.topface.ui.PaymentwallActivity;
import com.topface.topface.ui.fragments.PurchasesFragment;
import com.topface.topface.utils.CacheProfile;

import java.util.List;

public class PaymentWallBuyingFragment extends CoinsBuyingFragment {

    public static final String PAGE_TYPE = "page_type";

    public static CoinsBuyingFragment newInstance(String from, PaymentWallProducts.TYPE type) {
        PaymentWallBuyingFragment buyingFragment = new PaymentWallBuyingFragment();
        if (from != null) {
            Bundle args = new Bundle();
            args.putString(ARG_TAG_SOURCE, from);
            args.putInt(PAGE_TYPE, type.ordinal());
            buyingFragment.setArguments(args);
        }
        return buyingFragment;
    }

    public static CoinsBuyingFragment newInstance(int type, int coins, String from) {
        PaymentWallBuyingFragment fragment = new PaymentWallBuyingFragment();
        Bundle args = new Bundle();
        args.putInt(PurchasesFragment.ARG_ITEM_TYPE, type);
        args.putInt(PurchasesFragment.ARG_ITEM_PRICE, coins);
        if (from != null) {
            args.putString(ARG_TAG_SOURCE, from);
        }
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void initOpenIabHelper() {
        //Для PW нам не нужно иницилизировать OpenIAB, но нужно отнаследоваться от CoinsBuyingFragment
    }

    @Override
    public void buy(Products.BuyButton btn) {
        FragmentActivity activity = getActivity();
        if (activity != null) {
            activity.startActivityForResult(
                    PaymentwallActivity.getIntent(
                            activity,
                            btn.paymentwallLink
                    ),
                    PaymentwallActivity.ACTION_BUY
            );
        }
    }


    @Override
    public void onInAppBillingUnsupported() {
        //У нас всегда платежи доступны для PW
    }

    @Override
    public boolean isTestPurchasesAvailable() {
        return false;
    }

    @Override
    protected View getCoinsSubscriptionsButton(Products products, LinearLayout coinsButtonsContainer) {
        return null;
    }

    @Override
    protected List<Products.BuyButton> getCoinsProducts(Products products, boolean coinsMaskedExperiment) {
        return products.coins;
    }

    @Override
    public Products getProducts() {
        int type = getArguments().getInt(PAGE_TYPE);
        return CacheProfile.getPaymentWallProducts(
                PaymentWallProducts.TYPE.DIRECT.ordinal() == type ?
                        PaymentWallProducts.TYPE.DIRECT :
                        PaymentWallProducts.TYPE.MOBILE
        );
    }

    @Override
    public Products.BuyButtonClickListener getCoinsSubscriptionClickListener() {
        return null;
    }


}
