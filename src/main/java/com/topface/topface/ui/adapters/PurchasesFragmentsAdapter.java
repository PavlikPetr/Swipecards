package com.topface.topface.ui.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.util.SparseArrayCompat;
import com.topface.billing.BillingDriver;
import com.topface.billing.BillingFragment;
import com.topface.topface.ui.fragments.*;
import com.topface.topface.ui.fragments.buy.BuyingFragment;
import com.topface.topface.utils.Debug;

public class PurchasesFragmentsAdapter extends FragmentStatePagerAdapter {

    private final FragmentManager mFragmentManager;
    private SparseArrayCompat<PurchasesFragment.BuyingPageEntity> pagesInfo = new SparseArrayCompat<PurchasesFragment.BuyingPageEntity>();

    public PurchasesFragmentsAdapter(FragmentManager fm, SparseArrayCompat<PurchasesFragment.BuyingPageEntity> fragments) {
        super(fm);
        mFragmentManager = fm;
        pagesInfo = fragments;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return pagesInfo.get(position).getPageTitle();
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return BuyingFragment.newInstance(pagesInfo.get(position).getArguments().getString(BillingFragment.ARG_TAG_SOURCE));
            case 1:
                return FreeCoinsFragment.newInstance();
//            case 2:
//                return PaymentWallFragment.newInstance();
            default:
                try {
                    throw new Exception("wrong position");
                } catch (Exception e) {
                    Debug.error(e);

                }
                break;
        }

        return null;
    }

    @Override
    public int getCount() {
        return pagesInfo.size();
    }

    public SparseArrayCompat<PurchasesFragment.BuyingPageEntity> getPagesInfo() {
        return pagesInfo;
    }
}
