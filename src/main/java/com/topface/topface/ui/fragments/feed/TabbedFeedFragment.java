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

import com.topface.topface.R;
import com.topface.topface.banners.IPageWithAds;
import com.topface.topface.ui.adapters.TabbedFeedPageAdapter;
import com.topface.topface.ui.blocks.FloatBlock;
import com.topface.topface.ui.fragments.BaseFragment;
import com.topface.topface.ui.views.slidingtab.SlidingTabLayout;
import com.topface.topface.utils.CountersManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * base class for feeds with tabs
 */
public abstract class TabbedFeedFragment extends BaseFragment implements IPageWithAds {
    public static final String EXTRA_OPEN_PAGE = "openTabbedFeedAt";
    private static final String LAST_OPENED_PAGE = "last_opened_page";
    private ViewPager mPager;
    private SlidingTabLayout mSlidingTabLayout;
    private ArrayList<String> mPagesClassNames = new ArrayList<>();
    private ArrayList<String> mPagesTitles = new ArrayList<>();
    private ArrayList<Integer> mPagesCounters = new ArrayList<>();
    private FloatBlock mFloatBlock;

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
                        ((FeedFragment) fragment).finishMultiSelection();
                    }
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
        return root;
    }

    private void initPages(View root) {
        addPages();
        mPager = (ViewPager) root.findViewById(R.id.pager);
        mPager.setSaveEnabled(false);
        TabbedFeedPageAdapter bodyPagerAdapter = new TabbedFeedPageAdapter(getChildFragmentManager(),
                mPagesClassNames,
                mPagesTitles,
                mPagesCounters);
        mPager.setAdapter(bodyPagerAdapter);

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
        initFloatBlock((ViewGroup) view);
    }

    protected void initFloatBlock(ViewGroup view) {
        mFloatBlock = new FloatBlock(this, view);
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
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mFloatBlock != null) {
            mFloatBlock.onResume();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mPager != null) {
            setLastOpenedPage(mPager.getCurrentItem());
        }
        mPager = null;

        if (mFloatBlock != null) {
            mFloatBlock.onDestroy();
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
    public String getPageName() {
        return "";
    }

    @Override
    public ViewGroup getContainerForAd() {
        View view  = getView();
        if (view != null) {
            return (ViewGroup) getView().findViewById(R.id.loBannerContainer);
        }
        return null;
    }
}
