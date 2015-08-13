package com.topface.topface.utils;

import com.topface.topface.ui.fragments.buy.IFreeBuyVipFragment;

/**
 * Created by ppetr on 23.07.15.
 * hold method to return right class buying vip fragment
 */
public class BuyVipFragmentManager {

    public static String getClassName() {
        return IFreeBuyVipFragment.class.getName();
    }

    public static IFreeBuyVipFragment getVipInstance(boolean needActionBar, String from, String text) {
        return IFreeBuyVipFragment.newInstance(needActionBar, from, text);
    }
}
