package com.topface.topface.ui.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.HackyFragmentStatePagerAdapter;

import com.topface.billing.MarketApiType;
import com.topface.framework.utils.Debug;
import com.topface.topface.App;
import com.topface.topface.BuildConfig;
import com.topface.topface.R;
import com.topface.topface.data.PaymentWallProducts;
import com.topface.topface.ui.fragments.buy.VipPaymentWallBuyFragment;
import com.topface.topface.ui.fragments.profile.AbstractProfileFragment;
import com.topface.topface.utils.BuyVipFragmentManager;
import com.topface.topface.utils.GoogleMarketApiManager;

import java.util.ArrayList;

public class ProfilePageAdapter extends HackyFragmentStatePagerAdapter {

    private ArrayList<String> mFragmentsClasses = new ArrayList<>();
    private ArrayList<String> mFragmentsTitles = new ArrayList<>();
    private AbstractProfileFragment.ProfileInnerUpdater mProfileUpdater;

    public ProfilePageAdapter(FragmentManager fm, ArrayList<String> fragmentsClasses, ArrayList<String> fragmentTitles, AbstractProfileFragment.ProfileInnerUpdater profileUpdater) {
        super(fm);
        mFragmentsClasses = fragmentsClasses;
        mFragmentsTitles = fragmentTitles;
        mProfileUpdater = profileUpdater;
    }

    public int getFragmentIndexByClassName(String className) {
        for (int i = 0; i < mFragmentsClasses.size(); i++) {
            if (mFragmentsClasses.get(i).equals(className)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public int getCount() {
        return mFragmentsClasses.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if (!mFragmentsTitles.isEmpty())
            return mFragmentsTitles.get(position);

        return super.getPageTitle(position);
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment = null;
        try {
            String fragmentClassName = mFragmentsClasses.get(position);
            //create fragments
            if (fragmentClassName.equals(BuyVipFragmentManager.getClassName())) {
                //Если это платежи через Google Play, но у нас не поддерживаются Google Play Services,
                //то вместо покупок через GP показываем покупки через PaymentWall
                if (BuildConfig.MARKET_API_TYPE == MarketApiType.GOOGLE_PLAY && !new GoogleMarketApiManager().isMarketApiAvailable()) {
                    fragment = VipPaymentWallBuyFragment.newInstance(false, "ProfileTab", PaymentWallProducts.TYPE.DIRECT, App.getContext().getString(R.string.vip_state_off));
                } else {
                    fragment = BuyVipFragmentManager.getVipInstance(false, "ProfileTab", App.getContext().getString(R.string.vip_state_off));
                }
            } else {
                Class fragmentClass = Class.forName(fragmentClassName);
                fragment = (Fragment) fragmentClass.newInstance();
            }
            mProfileUpdater.bindFragment(fragment);
            mProfileUpdater.update();
        } catch (Exception ex) {
            Debug.error(ex);
        }
        return fragment;
    }

    public String getClassNameByPos(int pos){
        return mFragmentsClasses.get(pos);
    }

}
