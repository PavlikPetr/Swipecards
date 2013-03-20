package com.topface.topface.ui.blocks;

import android.support.v4.app.Fragment;
import android.view.ViewGroup;
import com.topface.topface.data.Options;
import com.topface.topface.ui.fragments.TopsFragment;
import com.topface.topface.ui.fragments.feed.DialogsFragment;
import com.topface.topface.ui.fragments.feed.LikesFragment;
import com.topface.topface.ui.fragments.feed.MutualFragment;
import com.topface.topface.ui.fragments.feed.VisitorsFragment;
import com.topface.topface.utils.CacheProfile;

import java.util.HashMap;
import java.util.Map;

/**
 * Блок для страниц, где нужно показывать баннеры или лидеров
 */
public class FloatBlock {
    private static Map<String, String> mFloatTypeMap;
    private static Options mOptions;
    private Fragment mFragment;
    private BannerBlock mBanner;
    private final ViewGroup mLayout;

    public FloatBlock(Fragment fragment, ViewGroup layoutView) {
        super();
        mOptions = CacheProfile.getOptions();
        mFragment = fragment;
        mFloatTypeMap = new HashMap<String, String>();
        mLayout = layoutView;
        setActivityMap();
        initBlock();
    }

    private void initBlock() {
        String currentFragment = mFragment.getClass().toString();
        if (mFloatTypeMap.containsKey(currentFragment)) {
            String floatType = mFloatTypeMap.get(currentFragment);
            if (floatType.equals(Options.FLOAT_TYPE_BANNER)) {
                mBanner = new BannerBlock(mFragment, mLayout);
            } else if (floatType.equals(Options.FLOAT_TYPE_LEADERS)) {
                new LeadersBlock(mFragment, mLayout);
            }
            //mLeaders = new LeadersBlock(mActivity, mLayout);
        }
        //Если переданого активити нет в карте, то не инициализируем ни один блок
    }

    public static void setActivityMap() {
        mFloatTypeMap = new HashMap<String, String>();
        if (mOptions.pages.containsKey(Options.PAGE_LIKES))
            mFloatTypeMap.put(LikesFragment.class.toString(), mOptions.pages.get(Options.PAGE_LIKES).floatType);
        if (mOptions.pages.containsKey(Options.PAGE_MUTUAL))
            mFloatTypeMap.put(MutualFragment.class.toString(), mOptions.pages.get(Options.PAGE_MUTUAL).floatType);
        if (mOptions.pages.containsKey(Options.PAGE_TOP))
            mFloatTypeMap.put(TopsFragment.class.toString(), mOptions.pages.get(Options.PAGE_TOP).floatType);
        if (mOptions.pages.containsKey(Options.PAGE_DIALOGS))
            mFloatTypeMap.put(DialogsFragment.class.toString(), mOptions.pages.get(Options.PAGE_DIALOGS).floatType);
        if (mOptions.pages.containsKey(Options.PAGE_VISITORS))
            mFloatTypeMap.put(VisitorsFragment.class.toString(), mOptions.pages.get(Options.PAGE_VISITORS).floatType);
    }

    public void onResume() {
        if (mBanner != null) mBanner.onResume();
    }

    public void onPause() {
        if (mBanner != null) mBanner.onPause();
    }

}
