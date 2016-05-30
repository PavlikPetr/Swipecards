package com.topface.topface.ui.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.HackyFragmentStatePagerAdapter;

import com.topface.framework.utils.Debug;
import com.topface.topface.ui.fragments.feed.FeedFragment;

import java.util.ArrayList;

public class TabbedFeedPageAdapter extends HackyFragmentStatePagerAdapter {
    private ArrayList<Integer> mFragmentsCounters = new ArrayList<>();
    private ArrayList<String> mFragmentsClasses = new ArrayList<>();
    private ArrayList<String> mFragmentsTitles = new ArrayList<>();

    public TabbedFeedPageAdapter(FragmentManager fm,
                                 ArrayList<String> fragmentsClasses,
                                 ArrayList<String> fragmentTitles,
                                 ArrayList<Integer> fragmentsCounters) {
        super(fm);
        mFragmentsClasses = fragmentsClasses;
        mFragmentsTitles = fragmentTitles;
        mFragmentsCounters = fragmentsCounters;
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment = null;
        try {
            String fragmentClassName = mFragmentsClasses.get(position);

            Class fragmentClass = Class.forName(fragmentClassName);
            fragment = (Fragment) fragmentClass.newInstance();
            if (fragment instanceof FeedFragment) {
                ((FeedFragment) fragment).setNeedTitles(false);
            }

        } catch (Exception ex) {
            Debug.error(ex);
        }
        return fragment;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if (!mFragmentsTitles.isEmpty())
            return mFragmentsTitles.get(position);

        return super.getPageTitle(position);
    }

    public int getPageCounter(int position) {
        if (!mFragmentsCounters.isEmpty()) {
            return mFragmentsCounters.get(position);
        }
        return 0;
    }

    public String getClassNameByPos(int pos) {
        return mFragmentsClasses.get(pos);
    }

    @Override
    public int getCount() {
        return mFragmentsClasses.size();
    }
}
