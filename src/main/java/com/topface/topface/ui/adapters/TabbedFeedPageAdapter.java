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

    // used to determine which item update will not be blocked at creation time
    // once used it will be reseted to -1
    private int mUnlockItemUpdateAtStart = -1;

    public TabbedFeedPageAdapter(FragmentManager fm,
                                 ArrayList<String> fragmentsClasses,
                                 ArrayList<String> fragmentTitles,
                                 ArrayList<Integer> fragmentsCounters) {
        super(fm);
        mFragmentsClasses = fragmentsClasses;
        mFragmentsTitles = fragmentTitles;
        mFragmentsCounters = fragmentsCounters;
    }

    /**
     * Unlocks update possibility for item specified by index at creation time.
     * This must be called before adapter returns first elements
     * and only one time
     *
     * @param unlockItemUpdateAtStart index of item
     */
    public void setUnlockItemUpdateAtStart(int unlockItemUpdateAtStart) {
        mUnlockItemUpdateAtStart = unlockItemUpdateAtStart;
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

                // by default - all items in tabs will be with blocked update possibility
                // excepting one - which must load content at creation time, because its visible to user
                if (mUnlockItemUpdateAtStart >= 0 && position == mUnlockItemUpdateAtStart) {
                    // once we've found item, which must load content
                    // we will forget this special position
                    mUnlockItemUpdateAtStart = -1;
                }
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

    @Override
    public int getCount() {
        return mFragmentsClasses.size();
    }
}
