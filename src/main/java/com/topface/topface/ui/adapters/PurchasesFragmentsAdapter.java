package com.topface.topface.ui.adapters;

import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.util.SparseArrayCompat;

import com.topface.billing.BillingFragment;
import com.topface.topface.data.Options;
import com.topface.topface.ui.fragments.*;
import com.topface.topface.ui.fragments.buy.GPlayBuyingFragment;
import com.topface.topface.ui.fragments.buy.PaymentWallBuyingFragment;
import com.topface.topface.ui.fragments.buy.VipBuyFragment;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Debug;

import java.util.LinkedList;

public class PurchasesFragmentsAdapter extends FragmentStatePagerAdapter {

    private final boolean mIsVip;
    private final int mCount;
    private SparseArrayCompat<Fragment> mFragmentCache = new SparseArrayCompat<>();
    private Bundle mArguments;

    public PurchasesFragmentsAdapter(FragmentManager fm, Bundle arguments, int count) {
        super(fm);
        mArguments = arguments;
        mFragmentCache = new SparseArrayCompat<>();
        mIsVip = arguments.getBoolean(PurchasesFragment.IS_VIP_PRODUCTS);
        mCount = count;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return CacheProfile.getOptions().tabs.get(position).name;
    }

    @Override
    public Fragment getItem(int position) {
        LinkedList<Options.Tab> tabs = CacheProfile.getOptions().tabs;
        Fragment fragment = mFragmentCache.get(position);
        if (fragment != null) return fragment;
        switch (tabs.get(position).type) {
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
                fragment = PaymentWallBuyingFragment.newInstance(mArguments.getString(BillingFragment.ARG_TAG_SOURCE));
                break;
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
        return mCount;
    }
}
