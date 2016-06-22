package com.topface.topface.ui.adapters;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.HackyFragmentStatePagerAdapter;

import com.topface.framework.utils.Debug;
import com.topface.topface.data.PaymentWallProducts;
import com.topface.topface.data.PurchasesTabData;
import com.topface.topface.ui.fragments.BonusFragment;
import com.topface.topface.ui.fragments.PurchasesFragment;
import com.topface.topface.ui.fragments.buy.MarketBuyingFragment;
import com.topface.topface.ui.fragments.buy.PaymentWallBuyingFragment;
import com.topface.topface.ui.fragments.buy.PurchasesConstants;
import com.topface.topface.ui.fragments.buy.VipBuyFragment;
import com.topface.topface.ui.fragments.buy.VipPaymentWallBuyFragment;

import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;

public class PurchasesFragmentsAdapter extends HackyFragmentStatePagerAdapter {

    private final boolean mIsVip;
    private Bundle mArguments;
    private LinkedList<PurchasesTabData> mTabs;

    public PurchasesFragmentsAdapter(FragmentManager fm, Bundle arguments, LinkedList<PurchasesTabData> tabs) {
        super(fm);
        mArguments = arguments;
        mIsVip = arguments.getBoolean(PurchasesFragment.IS_VIP_PRODUCTS);
        mTabs = tabs;
    }

    public boolean hasTab(String tabName) {
        for (PurchasesTabData tab : mTabs) {
            if (tab.type.equals(tabName)) {
                return true;
            }
        }
        return false;
    }

    public int getTabIndex(String tabName) {
        if (hasTab(tabName)) {
            for (PurchasesTabData tab : mTabs) {
                if (tab.type.equals(tabName)) {
                    return mTabs.indexOf(tab);
                }
            }
        }
        return -1;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mTabs.get(position).getUpperCaseName();
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment = null;
        String from = mArguments.getString(PurchasesConstants.ARG_TAG_SOURCE);
        String text = mArguments.getString(PurchasesConstants.ARG_RESOURCE_INFO_TEXT);
        switch (mTabs.get(position).type) {
            case PurchasesTabData.GPLAY:
                fragment = !mIsVip ? MarketBuyingFragment.newInstance(from, text) : VipBuyFragment.newInstance(true, from, text);
                break;
            case PurchasesTabData.BONUS:
                fragment = !mIsVip ? BonusFragment.newInstance(false) : null;
                break;
            case PurchasesTabData.PWALL:
                fragment = !mIsVip ? PaymentWallBuyingFragment.newInstance(from, PaymentWallProducts.TYPE.DIRECT, text) : VipPaymentWallBuyFragment.newInstance(true, from, PaymentWallProducts.TYPE.DIRECT, text);
                break;
            case PurchasesTabData.PWALL_MOBILE:
                fragment = !mIsVip ? PaymentWallBuyingFragment.newInstance(from, PaymentWallProducts.TYPE.MOBILE, text) : VipPaymentWallBuyFragment.newInstance(true, from, PaymentWallProducts.TYPE.MOBILE, text);
            default:
                Debug.error("PurchasesFragmentsAdapter wrong position");
                break;
        }
        return fragment;
    }

    @Nullable
    public String getClassNameByPos(int pos) {
        Class cls = null;
        if (mTabs.isEmpty() || pos < 0 || mTabs.size() < pos) {
            return null;
        }
        switch (mTabs.get(pos).type) {
            case PurchasesTabData.GPLAY:
                cls = !mIsVip ? MarketBuyingFragment.class : VipBuyFragment.class;
                break;
            case PurchasesTabData.BONUS:
                cls = !mIsVip ? BonusFragment.class : null;
                break;
            case PurchasesTabData.PWALL:
            case PurchasesTabData.PWALL_MOBILE:
                cls = !mIsVip ? PaymentWallBuyingFragment.class : VipPaymentWallBuyFragment.class;
                break;
        }
        return cls != null ? cls.getName() : null;
    }

    @Override
    public int getCount() {
        return mTabs.size();
    }
}
