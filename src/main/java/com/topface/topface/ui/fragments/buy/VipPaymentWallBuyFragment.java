package com.topface.topface.ui.fragments.buy;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.topface.topface.data.PaymentWallProducts;
import com.topface.topface.data.Products;
import com.topface.topface.ui.PaymentwallActivity;
import com.topface.topface.utils.CacheProfile;

public class VipPaymentWallBuyFragment extends VipBuyFragment {

    public static VipPaymentWallBuyFragment newInstance(boolean needActionBar, String from, PaymentWallProducts.TYPE type) {
        VipPaymentWallBuyFragment fragment = new VipPaymentWallBuyFragment();
        Bundle args = new Bundle();
        args.putBoolean(ACTION_BAR_CONST, needActionBar);
        args.putInt(PaymentWallBuyingFragment.PAGE_TYPE, type.ordinal());
        if (from != null) {
            args.putString(ARG_TAG_SOURCE, from);
        }
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected Products getProducts() {
        int type = getArguments().getInt(PaymentWallBuyingFragment.PAGE_TYPE);
        return CacheProfile.getPaymentWallProducts(PaymentWallProducts.TYPE.DIRECT.ordinal() == type ? PaymentWallProducts.TYPE.DIRECT : PaymentWallProducts.TYPE.MOBILE);

    }

    @Override
    protected void buy(String id, Products.BuyButton btn) {
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
        //Покупки вип у нас всегда поддеоживаются
    }

    @Override
    public void onSubscriptionUnsupported() {
        //Покупки на вип у нас всегда поддерживаются
    }

    @Override
    public boolean isTestPurchasesAvailable() {
        return false;
    }
}
