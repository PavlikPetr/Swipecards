package com.topface.topface.ui.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.util.SparseArrayCompat;

import com.topface.billing.BillingFragment;
import com.topface.topface.data.Options;
import com.topface.topface.ui.fragments.*;
import com.topface.topface.ui.fragments.buy.GPlayBuyingFragment;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Debug;

import java.util.LinkedList;

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
        return CacheProfile.getOptions().tabs.get(position).name;
    }

    @Override
    public Fragment getItem(int position) {
        LinkedList<Options.Tab> tabs = CacheProfile.getOptions().tabs;
        switch (tabs.get(position).type) {
            case Options.Tab.GPLAY:
                return GPlayBuyingFragment.newInstance(pagesInfo.get(position).getArguments().getString(BillingFragment.ARG_TAG_SOURCE));
            case Options.Tab.BONUS:
                return new BonusFragment();
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
