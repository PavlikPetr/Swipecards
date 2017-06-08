package com.topface.topface.ui.fragments.feed;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.banners.BannersController;
import com.topface.topface.banners.IBannerAds;
import com.topface.topface.data.CountersData;
import com.topface.topface.state.CountersDataProvider;
import com.topface.topface.statistics.FlurryUtils;
import com.topface.topface.ui.ChatActivity;
import com.topface.topface.ui.ITabLayoutHolder;
import com.topface.topface.ui.adapters.TabbedFeedPageAdapter;
import com.topface.topface.ui.dialogs.new_rate.RateAppFragment;
import com.topface.topface.ui.fragments.BaseFragment;
import com.topface.topface.ui.fragments.ChatFragment;
import com.topface.topface.ui.fragments.feed.feed_base.FeedNavigator;
import com.topface.topface.ui.views.TabLayoutCreator;
import com.topface.topface.utils.IActivityDelegate;
import com.topface.topface.utils.IStateSaverRegistrator;
import com.topface.topface.utils.IStateSaverRegistratorKt;
import com.topface.topface.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * base class for feeds with tabs
 */
public abstract class TabbedFeedFragment extends BaseFragment implements IBannerAds {
    public static final String HAS_FEED_AD = "com.topface.topface.has_feed_ad";
    public static final String EXTRA_OPEN_PAGE = "openTabbedFeedAt";
    private static final String LAST_OPENED_PAGE = "last_opened_page";
    private ViewPager mPager;
    private ArrayList<String> mPagesClassNames = new ArrayList<>();
    private ArrayList<String> mPagesTitles = new ArrayList<>();
    private ArrayList<Integer> mPagesCounters = new ArrayList<>();
    private BannersController mBannersController;
    private CountersDataProvider mCountersDataProvider;
    private TabbedFeedPageAdapter mBodyPagerAdapter;
    protected static int mVisitorsLastOpenedPage = 0;
    protected static int mLikesLastOpenedPage = 0;
    protected static int mDialogsLastOpenedPage = 0;
    protected CountersData mCountersData = new CountersData();
    private TabLayoutCreator mTabLayoutCreator;

    public static void setTabsDefaultPosition() {
        mVisitorsLastOpenedPage = 0;
        mLikesLastOpenedPage = 0;
        mDialogsLastOpenedPage = 0;
    }

    private ViewPager.OnPageChangeListener mPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            setLastOpenedPage(position);
            if (mTabLayoutCreator != null) {
                mTabLayoutCreator.setTabTitle(position);
            }
            if (mBodyPagerAdapter != null) {
                FlurryUtils.sendOpenEvent(mBodyPagerAdapter.getClassNameByPos(position));
            }
            List<Fragment> fragments = getChildFragmentManager().getFragments();
            if (fragments != null) {
                for (Fragment fragment : fragments) {
                    if (fragment instanceof FeedFragment) {
                        // clean multiselection, when switching tabs
                        ((FeedFragment) fragment).finishMultiSelection();
                    }
                }
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    private BroadcastReceiver mHasFeedAdReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            View containerForAd = getContainerForAd();
            if (containerForAd != null) {
                containerForAd.setVisibility(View.GONE);
            }
        }
    };

    protected abstract void onBeforeCountersUpdate(CountersData countersData);

    private void updateCounters(CountersData countersData) {
        mCountersData = countersData;
        onBeforeCountersUpdate(countersData);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View root = inflater.inflate(R.layout.fragment_tabbed_feed, null);
        bindView(root);
        initPages(root);
        mCountersDataProvider = new CountersDataProvider(new CountersDataProvider.ICountersUpdater() {
            @Override
            public void onUpdateCounters(CountersData countersData) {
                updateCounters(countersData);
                if (mTabLayoutCreator != null) {
                    mTabLayoutCreator.setTabTitle(getLastOpenedPage());
                }
            }
        });
        LocalBroadcastManager.getInstance(getActivity().getApplicationContext()).registerReceiver(mHasFeedAdReceiver, new IntentFilter(HAS_FEED_AD));
        mBannersController = new BannersController(this);
        IStateSaverRegistratorKt.registerLifeCycleDelegate(getActivity(), mBannersController);
        return root;
    }


    private void initPages(View root) {
        addPages();
        FragmentManager fm = getChildFragmentManager();
        if (fm.getFragments() != null) {
            for (Fragment fragment : fm.getFragments()) {
                if (fragment != null) {
                    fm.beginTransaction().remove(fragment).commit();
                }
            }
        }
        mPager = (ViewPager) root.findViewById(R.id.pager);
        mPager.setSaveEnabled(false);
        mBodyPagerAdapter = new TabbedFeedPageAdapter(getChildFragmentManager(),
                mPagesClassNames,
                mPagesTitles,
                mPagesCounters);
        mPager.setAdapter(mBodyPagerAdapter);
        mPager.addOnPageChangeListener(mPageChangeListener);

        Activity activity = getActivity();
        TabLayout tabLayout = null;
        if (activity instanceof ITabLayoutHolder) {
            tabLayout = ((ITabLayoutHolder) activity).getTabLayout();
        }
        if (tabLayout != null) {
            mTabLayoutCreator = new TabLayoutCreator(activity, mPager, tabLayout, mPagesTitles, mPagesCounters, mPagesClassNames);
        } else {
            throw new IllegalStateException("TabbedFeedFragment:: activity must have TabLayout");
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        int lastPage = getLastOpenedPage();
        if (savedInstanceState != null) {
            lastPage = savedInstanceState.getInt(LAST_OPENED_PAGE, getLastOpenedPage());
        } else {
            Intent i = getActivity().getIntent();
            String sLastPage = i.getStringExtra(EXTRA_OPEN_PAGE);
            if (!TextUtils.isEmpty(sLastPage) && mPagesClassNames.contains(sLastPage)) {
                lastPage = mPagesClassNames.indexOf(sLastPage);
            }
        }
        mPager.setCurrentItem(lastPage);
        mPageChangeListener.onPageSelected(lastPage);
    }

    protected abstract void addPages();

    protected void addBodyPage(String className, String pageTitle, int counter) {
        mPagesCounters.add(counter);
        mPagesTitles.add(pageTitle.toUpperCase(Locale.getDefault()));
        mPagesClassNames.add(className);
    }

    protected void updatePageCounter(String pageClassName, int counter) {
        mPagesCounters.set(mPagesClassNames.indexOf(pageClassName), counter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mCountersDataProvider.unsubscribe();
        LocalBroadcastManager.getInstance(getActivity().getApplicationContext()).unregisterReceiver(mHasFeedAdReceiver);
        mHasFeedAdReceiver = null;
        IStateSaverRegistratorKt.unregisterLifeCycleDelegate(getActivity(), mBannersController);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mPager != null) {
            setLastOpenedPage(mPager.getCurrentItem());
        }
        mPager = null;
    }

    @Override
    protected boolean isTabbedFragment() {
        return true;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(LAST_OPENED_PAGE, mPager != null ? mPager.getCurrentItem() : 0);
    }

    /**
     * All children must have static field, which is holding last opened page index
     * for handling viewpager current item while app is runnig
     *
     * @return index of last opened page
     */
    protected abstract int getLastOpenedPage();

    protected abstract void setLastOpenedPage(int lastOpenedPage);

    @Override
    public ViewGroup getContainerForAd() {
        View view = getView();
        if (view != null) {
            return (ViewGroup) getView().findViewById(R.id.banner_container_for_tabbed_feeds);
        }
        return null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Utils.activityResultToNestedFragments(getChildFragmentManager(), requestCode, resultCode, data);
        if (requestCode == ChatActivity.REQUEST_CHAT && data != null && data.hasExtra(ChatFragment.MUTUAL)) {
            if (data.getBooleanExtra(ChatFragment.MUTUAL, false) && RateAppFragment.Companion.isApplicable(App.get().getOptions().ratePopupNewVersion)) {
                (new FeedNavigator((IActivityDelegate) getActivity())).showRateAppFragment();
            }
        }
    }

    @Override
    public boolean isTrackable() {
        return false;
    }
}
