package com.topface.topface.ui.fragments.feed;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.ui.adapters.TabbedLikesPageAdapter;
import com.topface.topface.ui.fragments.BaseFragment;
import com.topface.topface.ui.views.slidingtab.SlidingTabLayout;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.CountersManager;
import com.topface.topface.utils.config.AppConfig;

import java.util.ArrayList;
import java.util.List;

public class TabbedLikesFragment extends BaseFragment {
    private static final String LAST_OPENED_PAGE = "last_opened_page";
    private ViewPager mPager;
    private SlidingTabLayout mSlidingTabLayout;
    private ArrayList<String> mPagesClassNames = new ArrayList<>();
    private ArrayList<String> mPagesTitles = new ArrayList<>();
    private ArrayList<Integer> mPagesCounters = new ArrayList<>();
    private ViewPager.OnPageChangeListener mPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            List<android.support.v4.app.Fragment> fragments = getChildFragmentManager().getFragments();
            for (android.support.v4.app.Fragment fragment : fragments) {
                if (fragment instanceof FeedFragment) {
                    ((FeedFragment) fragment).finishMultiSelection();
                }
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    private BroadcastReceiver mCountersReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            onCountersUpdated();
        }
    };

    private void onCountersUpdated() {
        mPagesCounters.set(mPagesClassNames.indexOf(LikesFragment.class.getName()), CacheProfile.unread_likes);
        mPagesCounters.set(mPagesClassNames.indexOf(MutualFragment.class.getName()), CacheProfile.unread_mutual);
        mPagesCounters.set(mPagesClassNames.indexOf(AdmirationFragment.class.getName()), CacheProfile.unread_admirations);

        if (mSlidingTabLayout != null) {
            mSlidingTabLayout.updateTitles();
        }
    }

    @Override
    protected String getTitle() {
        return getString(R.string.general_sympathies);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_tabbed_likes, null);

        initPages(root);

        return root;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (savedInstanceState != null) {
            mPager.setCurrentItem(savedInstanceState.getInt(LAST_OPENED_PAGE, 0));
        }
    }

    private void initPages(View root) {
        addBodyPage(LikesFragment.class.getName(), getString(R.string.general_likes), CacheProfile.unread_likes);
        addBodyPage(MutualFragment.class.getName(), getString(R.string.general_mutual), CacheProfile.unread_mutual);
        addBodyPage(AdmirationFragment.class.getName(), getString(R.string.general_admirations), CacheProfile.unread_admirations);

        mPager = (ViewPager) root.findViewById(R.id.pager);

        mPager.setSaveEnabled(false);
        TabbedLikesPageAdapter bodyPagerAdapter = new TabbedLikesPageAdapter(getChildFragmentManager(),
                mPagesClassNames,
                mPagesTitles,
                mPagesCounters);
        mPager.setAdapter(bodyPagerAdapter);

        mSlidingTabLayout = (SlidingTabLayout) root.findViewById(R.id.sliding_tabs);
        mSlidingTabLayout.setUseWeightProportions(true);
        mSlidingTabLayout.setCustomTabView(R.layout.tab_indicator, R.id.tab_title, R.id.tab_counter);
        mSlidingTabLayout.setViewPager(mPager);
        // need this, because SlidingView defines its own listener
        mSlidingTabLayout.setOnPageChangeListener(mPageChangeListener);

        mPager.setCurrentItem(App.getAppConfig().getTabbedLikesLastPage());
    }

    private void addBodyPage(String className, String pageTitle, int counter) {
        mPagesCounters.add(counter);
        mPagesTitles.add(pageTitle.toUpperCase());
        mPagesClassNames.add(className);
    }

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(getActivity())
                .registerReceiver(mCountersReceiver, new IntentFilter(CountersManager.UPDATE_COUNTERS));
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity())
                .unregisterReceiver(mCountersReceiver);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        AppConfig appConfig = App.getAppConfig();
        appConfig.setTabbedLikesLastPage(mPager.getCurrentItem());
        appConfig.saveConfig();
        mPager = null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(LAST_OPENED_PAGE, mPager.getCurrentItem());
    }

}
