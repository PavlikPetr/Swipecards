package com.topface.topface.ui.fragments.buy;

public class AmazonBuyingFragment extends GooglePlayBuyingFragment {

    public static AmazonBuyingFragment newInstance(int type, int coins, String from) {
        AmazonBuyingFragment fragment = new AmazonBuyingFragment();
        setArguments(type, coins, from, fragment);
        return fragment;
    }

    @Override
    public boolean isTestPurchasesAvailable() {
        return false;
    }
}
