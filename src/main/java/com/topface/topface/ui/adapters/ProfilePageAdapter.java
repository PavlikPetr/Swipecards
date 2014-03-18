package com.topface.topface.ui.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.util.SparseArrayCompat;

import com.topface.topface.ui.fragments.profile.AbstractProfileFragment;
import com.topface.topface.ui.fragments.profile.HeaderMainFragment;
import com.topface.topface.ui.fragments.profile.HeaderStatusFragment;
import com.topface.topface.utils.Debug;
import com.viewpagerindicator.PageIndicator;

import java.util.ArrayList;

public class ProfilePageAdapter extends FragmentStatePagerAdapter {

    private ArrayList<String> mFragmentsClasses = new ArrayList<>();
    private ArrayList<String> mFragmentsTitles = new ArrayList<>();
    private SparseArrayCompat<Fragment> mFragmentCache = new SparseArrayCompat<>();
    private AbstractProfileFragment.ProfileInnerUpdater mProfileUpdater;
    private PageIndicator mPageIndicator;

    public ProfilePageAdapter(FragmentManager fm, ArrayList<String> fragmentsClasses, AbstractProfileFragment.ProfileInnerUpdater profileUpdater) {
        super(fm);
        mFragmentsClasses = fragmentsClasses;
        mProfileUpdater = profileUpdater;
    }

    public ProfilePageAdapter(FragmentManager fm, ArrayList<String> fragmentsClasses, ArrayList<String> fragmentTitles, AbstractProfileFragment.ProfileInnerUpdater profileUpdater) {
        super(fm);
        mFragmentsClasses = fragmentsClasses;
        mFragmentsTitles = fragmentTitles;
        mProfileUpdater = profileUpdater;
    }

    public SparseArrayCompat<Fragment> getFragmentCache() {
        return mFragmentCache;
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
        Fragment fragment = mFragmentCache.get(position);
        if (fragment != null) return fragment;
        try {
            String fragmentClassName = mFragmentsClasses.get(position);
            //create fragments
            if (fragmentClassName.equals(HeaderMainFragment.class.getName())) {
                fragment = HeaderMainFragment.newInstance(mProfileUpdater.getProfile());
            } else if (fragmentClassName.equals(HeaderStatusFragment.class.getName())) {
                fragment = HeaderStatusFragment.newInstance(mProfileUpdater.getProfile(), mProfileUpdater.getProfileType());
            } else {
                Class fragmentClass = Class.forName(fragmentClassName);
                fragment = (Fragment) fragmentClass.newInstance();
            }
            mProfileUpdater.bindFragment(fragment);
            mProfileUpdater.update();
        } catch (Exception ex) {
            Debug.error(ex);
        }
        mFragmentCache.put(position, fragment);
        return fragment;
    }

    public void removeItem(int position) {
        mFragmentsClasses.remove(position);
        if (position >= 0 && position < mFragmentsTitles.size())
            mFragmentsTitles.remove(position);
        notifyDataSetChanged();
        if (mPageIndicator != null) mPageIndicator.notifyDataSetChanged();
    }

    public void removeItem(String className) {
        int position = getFragmentIndexByClassName(className);
        if (position >= 0 && position < getCount()) {
            removeItem(position);
        }
    }

    public void setPageIndicator(PageIndicator indicator) {
        mPageIndicator = indicator;
    }
}
