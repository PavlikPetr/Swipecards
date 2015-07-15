package com.topface.topface.ui.fragments.feed;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import com.topface.topface.R;
import com.topface.topface.banners.BannersController;
import com.topface.topface.banners.PageInfo;
import com.topface.topface.banners.RefreshablePageWithAds;
import com.topface.topface.banners.ad_providers.IRefresher;
import com.topface.topface.ui.adapters.FeedAdapter;
import com.topface.topface.ui.adapters.TabbedFeedPageAdapter;
import com.topface.topface.ui.fragments.BaseFragment;
import com.topface.topface.ui.views.slidingtab.SlidingTabLayout;
import com.topface.topface.utils.CountersManager;
import com.topface.topface.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * base class for feeds with tabs
 */
public abstract class TabbedFeedFragment extends BaseFragment implements RefreshablePageWithAds {
    public static final String HAS_FEED_AD = "com.topface.topface.has_feed_ad";
    public static final String EXTRA_OPEN_PAGE = "openTabbedFeedAt";
    private static final String LAST_OPENED_PAGE = "last_opened_page";
    private ViewPager mPager;
    private SlidingTabLayout mSlidingTabLayout;
    private ArrayList<String> mPagesClassNames = new ArrayList<>();
    private ArrayList<String> mPagesTitles = new ArrayList<>();
    private ArrayList<Integer> mPagesCounters = new ArrayList<>();
    private BannersController mBannersController;

    private TabbedFeedPageAdapter mBodyPagerAdapter;

    protected static int mVisitorsastOpenedPage = 0;
    protected static int mLikesLastOpenedPage = 0;
    protected static int mDialogsLastOpenedPage = 0;

    public static void setTabsDefaultPosition() {
        mVisitorsastOpenedPage = 0;
        mLikesLastOpenedPage = 0;
        mDialogsLastOpenedPage = 0;
    }


    private ViewPager.OnPageChangeListener mPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
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

    private BroadcastReceiver mCountersReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            onCountersUpdated();
        }
    };

    protected abstract void onBeforeCountersUpdate();

    private void onCountersUpdated() {
        onBeforeCountersUpdate();

        if (mSlidingTabLayout != null) {
            mSlidingTabLayout.updateTitles();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_tabbed_feed, null);
        initPages(root);
        LocalBroadcastManager.getInstance(getActivity())
                .registerReceiver(mCountersReceiver, new IntentFilter(CountersManager.UPDATE_COUNTERS));
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mHasFeedAdReceiver, new IntentFilter(HAS_FEED_AD));
        return root;
    }

    private void initPages(View root) {
        addPages();
        mPager = (ViewPager) root.findViewById(R.id.pager);
        mPager.setSaveEnabled(false);
        mBodyPagerAdapter = new TabbedFeedPageAdapter(getChildFragmentManager(),
                mPagesClassNames,
                mPagesTitles,
                mPagesCounters);
        mPager.setAdapter(mBodyPagerAdapter);

        mSlidingTabLayout = (SlidingTabLayout) root.findViewById(R.id.sliding_tabs);
        mSlidingTabLayout.setUseWeightProportions(true);
        mSlidingTabLayout.setCustomTabView(getIndicatorLayout(), R.id.tab_title, R.id.tab_counter);
        mSlidingTabLayout.setViewPager(mPager);
        // need this, because SlidingView defines its own listener
        mSlidingTabLayout.setOnPageChangeListener(mPageChangeListener);
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
            if (!TextUtils.isEmpty(sLastPage)) {
                lastPage = mPagesClassNames.indexOf(sLastPage);
            }
        }
        mPager.setCurrentItem(lastPage);
        initFloatBlock();
    }

    public int getTabLayoutHeight() {
        if (mSlidingTabLayout != null) {
            return mSlidingTabLayout.getMeasuredHeight();
        }
        return 0;
    }

    protected void initFloatBlock() {
        Utils.addOnGlobalLayoutListener(mPager, new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                boolean needNativeAd = false;
                if (mBodyPagerAdapter != null) {
                    for (int i = 0; i < mBodyPagerAdapter.getCount(); i++) {
                        Fragment feed = mBodyPagerAdapter.getItem(i);
                        if (feed instanceof FeedFragment) {
                            FeedAdapter adapter = ((FeedFragment) feed).getListAdapter();
                            if (adapter != null && adapter.isNeedFeedAd()) {
                                needNativeAd = true;
                                break;
                            }
                        }
                    }
                }
                if (!needNativeAd) {
                    mBannersController = new BannersController(TabbedFeedFragment.this);
                }
            }
        });
    }

    protected abstract void addPages();

    protected int getIndicatorLayout() {
        return R.layout.tab_indicator;
    }

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
        LocalBroadcastManager.getInstance(getActivity())
                .unregisterReceiver(mCountersReceiver);
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mHasFeedAdReceiver);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mPager != null) {
            setLastOpenedPage(mPager.getCurrentItem());
        }
        mPager = null;
        if (mBannersController != null) {
            mBannersController.onDestroy();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mRefresher != null) {
            mRefresher.refreshBanner();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(LAST_OPENED_PAGE, mPager.getCurrentItem());
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
    public PageInfo.PageName getPageName() {
        return PageInfo.PageName.UNKNOWN_PAGE;
    }

    @Override
    public ViewGroup getContainerForAd() {
        View view = getView();
        if (view != null) {
            return (ViewGroup) getView().findViewById(R.id.banner_container_for_tabbed_feeds);
        }
        return null;
    }

    private IRefresher mRefresher;

    @Override
    public void setRefresher(IRefresher refresher) {
        mRefresher = refresher;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        for (Fragment fr : getChildFragmentManager().getFragments()) {
            if (fr != null) {
                fr.onActivityResult(requestCode, resultCode, data);
            }
        }
    }
}
