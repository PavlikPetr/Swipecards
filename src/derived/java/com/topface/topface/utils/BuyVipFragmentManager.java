package com.topface.topface.utils;


import com.topface.topface.data.PaymentWallProducts;
import com.topface.topface.ui.fragments.buy.VipBuyFragment;
import com.topface.topface.ui.fragments.buy.VipPaymentWallBuyFragment;

/**
 * Created by ppetr on 23.07.15.
 * hold method to return right class buying vip fragment
 */
public class BuyVipFragmentManager {

    public static String getClassName() {
        return VipPaymentWallBuyFragment.class.getName();
    }

    public static VipBuyFragment getVipInstance(boolean needActionBar, String from, String text) {
        return VipPaymentWallBuyFragment.newInstance(needActionBar, from, PaymentWallProducts.TYPE.MOBILE, text);
    }
}
