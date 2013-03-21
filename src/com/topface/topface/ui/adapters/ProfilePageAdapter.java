package com.topface.topface.ui.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import com.topface.topface.ui.fragments.ProfileFragment;
import com.topface.topface.ui.profile.ProfileBlackListControlFragment;
import com.topface.topface.utils.Debug;
import com.viewpagerindicator.CirclePageIndicator;
import com.viewpagerindicator.PageIndicator;

import java.util.ArrayList;
import java.util.HashMap;

public class ProfilePageAdapter extends FragmentStatePagerAdapter {

    private ArrayList<String> mFragmentsClasses = new ArrayList<String>();
    private ArrayList<String> mFragmentsTitles = new ArrayList<String>();
    private HashMap<Integer, Fragment> mFragmentCache = new HashMap<Integer, Fragment>();
    private ProfileFragment.ProfileUpdater mProfileUpdater;
    private PageIndicator mPageIndicator;

    public ProfilePageAdapter(FragmentManager fm, ArrayList<String> fragmentsClasses, ProfileFragment.ProfileUpdater profileUpdater) {
        super(fm);
        mFragmentsClasses = fragmentsClasses;
        mProfileUpdater = profileUpdater;
    }

    public ProfilePageAdapter(FragmentManager fm, ArrayList<String> fragmentsClasses, ArrayList<String> fragmentTitles, ProfileFragment.ProfileUpdater profileUpdater) {
        super(fm);
        mFragmentsClasses = fragmentsClasses;
        mFragmentsTitles = fragmentTitles;
        mProfileUpdater = profileUpdater;
    }

    public HashMap<Integer, Fragment> getFragmentCache() {
        return  mFragmentCache;
    }

    public int getFragmentIndexByClassName(String className) {
        for (int i = 0; i < mFragmentsClasses.size(); i++) {
            if (mFragmentsClasses.get(i).equals(className)) {
                return i;
            }
        }
        return -1;
    }

    public String getClassNameByFragmentIndex(int i) {
        if (mFragmentsClasses.isEmpty()) {
            return "";
        }
        return mFragmentsClasses.get(i);
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
        if (mFragmentCache.containsKey(position)) {
            return mFragmentCache.get(position);
        }
        try {
            String fragmentClassName = mFragmentsClasses.get(position);

            //create fragments
            if (fragmentClassName.equals(ProfileFragment.HeaderMainFragment.class.getName())) {
                fragment = ProfileFragment.HeaderMainFragment.newInstance(mProfileUpdater.getProfile());
            } else if (fragmentClassName.equals(ProfileFragment.HeaderStatusFragment.class.getName())) {
                fragment = ProfileFragment.HeaderStatusFragment.newInstance(mProfileUpdater.getProfile());
            } else if (fragmentClassName.equals(ProfileBlackListControlFragment.class.getName())) {
                fragment = ProfileBlackListControlFragment.newInstance(mProfileUpdater.getProfile().uid, mProfileUpdater.getProfile().inBlackList);
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

    public void setPageIndicator(PageIndicator indicator){
        mPageIndicator = indicator;
    }
}
