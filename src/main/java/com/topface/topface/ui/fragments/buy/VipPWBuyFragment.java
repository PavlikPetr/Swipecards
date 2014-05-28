package com.topface.topface.ui.fragments.buy;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.topface.billing.BillingDriver;
import com.topface.billing.PaymentwallBillingDriver;
import com.topface.topface.data.PaymentWallProducts;
import com.topface.topface.data.Products;
import com.topface.topface.ui.PaymentwallActivity;
import com.topface.topface.utils.CacheProfile;

public class VipPWBuyFragment extends VipBuyFragment {

    public static VipPWBuyFragment newInstance(boolean needActionBar, String text, String from, PaymentWallProducts.TYPE type) {
        VipPWBuyFragment fragment = new VipPWBuyFragment();
        Bundle args = new Bundle();
        args.putBoolean(ACTION_BAR_CONST, needActionBar);
        args.putInt(PaymentWallBuyingFragment.PAGE_TYPE, type.ordinal());
        if (text != null) {
            args.putString(ARG_TAG_EXRA_TEXT, text);
        }
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
    protected BillingDriver getBillingDriver() {
        return new PaymentwallBillingDriver(getActivity(), this);
    }

    @Override
    protected void buy(String id, Products.BuyButton btn) {
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
}
