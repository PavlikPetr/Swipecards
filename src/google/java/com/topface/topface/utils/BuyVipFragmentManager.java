package com.topface.topface.utils;

import android.os.Bundle;
import android.text.TextUtils;

import com.topface.topface.ui.fragments.buy.VipBuyFragment;

/**
 * Created by ppetr on 23.07.15.
 * hold method to return right class buying vip fragment
 */
public class BuyVipFragmentManager {

    public static String getClassName() {
        return VipBuyFragment.class.getName();
    }

    public static VipBuyFragment getVipInstance(boolean needActionBar, String from, String text){
        return VipBuyFragment.newInstance(needActionBar, from, text);
    }
}
