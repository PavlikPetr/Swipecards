package com.topface.topface.ui.fragments.buy;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.LinearLayout;

import com.topface.billing.BillingDriver;
import com.topface.billing.PaymentwallBillingDriver;
import com.topface.topface.data.PaymentWallProducts;
import com.topface.topface.data.Products;
import com.topface.topface.ui.PaymentwallActivity;
import com.topface.topface.utils.CacheProfile;

import java.util.List;

public class PaymentWallBuyingFragment extends AbstractBuyingFragment{

    public static final String PAGE_TYPE = "page_type";

    public static AbstractBuyingFragment newInstance(String from, PaymentWallProducts.TYPE type) {
        PaymentWallBuyingFragment buyingFragment = new PaymentWallBuyingFragment();
        if (from != null) {
            Bundle args = new Bundle();
            args.putString(ARG_TAG_SOURCE, from);
            args.putInt(PAGE_TYPE, type.ordinal());
            buyingFragment.setArguments(args);
        }
        return buyingFragment;
    }


    public static AbstractBuyingFragment newInstance(int type, int coins, String from) {
        PaymentWallBuyingFragment fragment = new PaymentWallBuyingFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_ITEM_TYPE, type);
        args.putInt(ARG_ITEM_PRICE, coins);
        if (from != null) {
            args.putString(ARG_TAG_SOURCE, from);
        }
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void buy(Products.BuyButton btn) {
        FragmentActivity activity = getActivity();
        if (activity != null) {
            activity.startActivityForResult(
                    PaymentwallActivity.getIntent(
                            activity,
                            isTestPurchasesEnabled(),
                            btn.pWallLink
                    ),
                    PaymentwallActivity.ACTION_BUY
            );
        }
    }

    @Override
    protected BillingDriver getBillingDriver() {
        return new PaymentwallBillingDriver(getActivity(), this);
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
        return CacheProfile.getPaymentWallProducts(PaymentWallProducts.TYPE.DIRECT.ordinal() == type ? PaymentWallProducts.TYPE.DIRECT : PaymentWallProducts.TYPE.MOBILE);
    }

    @Override
    public Products.BuyButtonClickListener getCoinsSubscriptionClickListener() {
        return null;
    }


}
