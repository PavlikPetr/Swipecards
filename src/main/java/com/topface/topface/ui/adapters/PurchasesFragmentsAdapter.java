package com.topface.topface.ui.adapters;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.util.SparseArrayCompat;

import com.topface.billing.OpenIabFragment;
import com.topface.framework.utils.Debug;
import com.topface.topface.data.Options;
import com.topface.topface.data.PaymentWallProducts;
import com.topface.topface.ui.fragments.BonusFragment;
import com.topface.topface.ui.fragments.PurchasesFragment;
import com.topface.topface.ui.fragments.buy.AmazonBuyingFragment;
import com.topface.topface.ui.fragments.buy.GooglePlayBuyingFragment;
import com.topface.topface.ui.fragments.buy.PaymentWallBuyingFragment;
import com.topface.topface.ui.fragments.buy.VipBuyFragment;
import com.topface.topface.ui.fragments.buy.VipPaymentWallBuyFragment;

import java.util.LinkedList;

public class PurchasesFragmentsAdapter extends FragmentStatePagerAdapter {

    private final boolean mIsVip;
    private SparseArrayCompat<Fragment> mFragmentCache = new SparseArrayCompat<>();
    private Bundle mArguments;
    private LinkedList<Options.Tab> mTabs;

    public PurchasesFragmentsAdapter(FragmentManager fm, Bundle arguments, LinkedList<Options.Tab> tabs) {
        super(fm);
        mArguments = arguments;
        mIsVip = arguments.getBoolean(PurchasesFragment.IS_VIP_PRODUCTS);
        mTabs = tabs;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mTabs.get(position).name;
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment = mFragmentCache.get(position);
        if (fragment != null) return fragment;
        String from = mArguments.getString(OpenIabFragment.ARG_TAG_SOURCE);
        switch (mTabs.get(position).type) {
            case Options.Tab.GPLAY:
                if (!mIsVip) {
                    fragment = GooglePlayBuyingFragment.newInstance(from);
                } else {
                    fragment = VipBuyFragment.newInstance(true, from);
                }
                break;
            case Options.Tab.AMAZON:
                if (!mIsVip) {
                    fragment = AmazonBuyingFragment.newInstance(from);
                } else {
                    fragment = VipBuyFragment.newInstance(true, from);
                }
                break;
            case Options.Tab.BONUS:
                if (!mIsVip) {
                    fragment = BonusFragment.newInstance(false);
                }
                break;
            case Options.Tab.PWALL:
                if (!mIsVip) {
                    fragment = PaymentWallBuyingFragment.newInstance(from, PaymentWallProducts.TYPE.DIRECT);
                } else {
                    fragment = VipPaymentWallBuyFragment.newInstance(true, from, PaymentWallProducts.TYPE.DIRECT);
                }
                break;
            case Options.Tab.PWALL_MOBILE:
                if (!mIsVip) {
                    fragment = PaymentWallBuyingFragment.newInstance(from, PaymentWallProducts.TYPE.MOBILE);
                } else {
                    fragment = VipPaymentWallBuyFragment.newInstance(true, from, PaymentWallProducts.TYPE.MOBILE);
                }
                break;
            default:
                Debug.error("PurchasesFragmentsAdapter wrong position");
                break;
        }
        mFragmentCache.put(position, fragment);
        return fragment;
    }

    @Override
    public int getCount() {
        return mTabs.size();
    }
}
