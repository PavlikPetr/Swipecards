package com.topface.topface.ui.adapters;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.util.SparseArrayCompat;

import com.topface.billing.BillingFragment;
import com.topface.framework.utils.Debug;
import com.topface.topface.data.Options;
import com.topface.topface.data.PaymentWallProducts;
import com.topface.topface.ui.fragments.*;
import com.topface.topface.ui.fragments.buy.GPlayBuyingFragment;
import com.topface.topface.ui.fragments.buy.PaymentWallBuyingFragment;
import com.topface.topface.ui.fragments.buy.VipBuyFragment;
import com.topface.topface.ui.fragments.buy.VipPWBuyFragment;

import java.util.LinkedList;

public class PurchasesFragmentsAdapter extends FragmentStatePagerAdapter {

    private final boolean mIsVip;
    private SparseArrayCompat<Fragment> mFragmentCache = new SparseArrayCompat<>();
    private Bundle mArguments;
    private LinkedList<Options.Tab> mTabs;

    public PurchasesFragmentsAdapter(FragmentManager fm, Bundle arguments, LinkedList<Options.Tab> tabs) {
        super(fm);
        mArguments = arguments;
        mFragmentCache = new SparseArrayCompat<>();
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
        switch (mTabs.get(position).type) {
            case Options.Tab.GPLAY:
                if (!mIsVip) {
                    fragment = GPlayBuyingFragment.newInstance(mArguments.getString(BillingFragment.ARG_TAG_SOURCE));
                } else {
                    fragment = VipBuyFragment.newInstance(true, mArguments.getString(VipBuyFragment.ARG_TAG_EXRA_TEXT), mArguments.getString(VipBuyFragment.ARG_TAG_SOURCE));
                }
                break;
            case Options.Tab.BONUS:
                if (!mIsVip) {
                    fragment = new BonusFragment();
                }
                break;
            case Options.Tab.PWALL:
                if (!mIsVip) {
                    fragment = PaymentWallBuyingFragment.newInstance(mArguments.getString(BillingFragment.ARG_TAG_SOURCE), PaymentWallProducts.TYPE.DIRECT);
                } else {
                    fragment = VipPWBuyFragment.newInstance(true, mArguments.getString(VipBuyFragment.ARG_TAG_EXRA_TEXT),
                            mArguments.getString(VipBuyFragment.ARG_TAG_SOURCE), PaymentWallProducts.TYPE.DIRECT);
                }
                break;
            case Options.Tab.PWALL_MOBILE:
                if (!mIsVip) {
                    fragment = PaymentWallBuyingFragment.newInstance(mArguments.getString(BillingFragment.ARG_TAG_SOURCE), PaymentWallProducts.TYPE.MOBILE);
                } else {
                    fragment = VipPWBuyFragment.newInstance(true, mArguments.getString(VipBuyFragment.ARG_TAG_EXRA_TEXT),
                            mArguments.getString(VipBuyFragment.ARG_TAG_SOURCE), PaymentWallProducts.TYPE.MOBILE);
                }
            default:
                try {
                    throw new Exception("wrong position");
                } catch (Exception e) {
                    Debug.error(e);
                }
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
