package com.topface.topface.ui.fragments.buy;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;

import com.topface.topface.data.PaymentWallProducts;
import com.topface.topface.data.Products;
import com.topface.topface.statistics.PushButtonVipStatistics;
import com.topface.topface.statistics.PushButtonVipUniqueStatistics;
import com.topface.topface.ui.PaymentwallActivity;
import com.topface.topface.ui.fragments.PurchasesFragment;
import com.topface.topface.utils.CacheProfile;

import java.util.List;

public class PaymentWallBuyingFragment extends CoinsBuyingFragment {

    public static final String PAGE_TYPE = "page_type";

    public static CoinsBuyingFragment newInstance(String from, PaymentWallProducts.TYPE type, String text, int visibility) {
        PaymentWallBuyingFragment buyingFragment = new PaymentWallBuyingFragment();
        Bundle args = new Bundle();
        if (!TextUtils.isEmpty(text)) {
            args.putString(ARG_RESOURCE_INFO_TEXT, text);
        }
        if (visibility != 0) {
            args.putInt(ARG_RESOURCE_INFO_VISIBILITY, visibility);
        }
        if (from != null) {
            args.putString(ARG_TAG_SOURCE, from);
        }
        if (type != null) {
            args.putInt(PAGE_TYPE, type.ordinal());
        }
        buyingFragment.setArguments(args);
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
    public void buy(Products.BuyButton btn) {
        PushButtonVipUniqueStatistics.sendPushButtonNoVip(btn.id, ((Object) this).getClass().getSimpleName(), getFrom());
        PushButtonVipStatistics.send(btn.id, ((Object) this).getClass().getSimpleName(), getFrom());
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
    public void onSubscriptionUnsupported() {
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
