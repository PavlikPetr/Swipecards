package com.topface.topface.ui.blocks;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.ViewGroup;
import com.topface.topface.data.Options;
import com.topface.topface.requests.ProfileRequest;
import com.topface.topface.ui.fragments.feed.*;
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
    private LeadersBlock mLeaders;
    private BroadcastReceiver mOptionsUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mFloatTypeMap = null;
            setActivityMap();
        }
    };

    public FloatBlock(Fragment fragment, ViewGroup layoutView) {
        super();
        mOptions = CacheProfile.getOptions();
        mFragment = fragment;
        mLayout = layoutView;
        setActivityMap();
        initBlock();
    }

    private void initBlock() {
        String currentFragment = mFragment.getClass().toString();
        if (mFloatTypeMap.containsKey(currentFragment)) {
            String floatType = mFloatTypeMap.get(currentFragment);
            if (floatType.equals(Options.FLOAT_TYPE_BANNER)) {
                if (!CacheProfile.show_ad) return;
                mBanner = new BannerBlock(mFragment, mLayout);
            } else if (floatType.equals(Options.FLOAT_TYPE_LEADERS)) {
                mLeaders = new LeadersBlock(mFragment, mLayout);
            }
            //mLeaders = new LeadersBlock(mActivity, mLayout);
        }
        //Если переданого активити нет в карте, то не инициализируем ни один блок
    }

    public static void setActivityMap() {
        if (mFloatTypeMap == null) {
            mFloatTypeMap = new HashMap<String, String>();
            if (mOptions.pages.containsKey(Options.PAGE_LIKES)) {
                mFloatTypeMap.put(LikesFragment.class.toString(), mOptions.pages.get(Options.PAGE_LIKES).floatType);
            }
            if (mOptions.pages.containsKey(Options.PAGE_MUTUAL)) {
                mFloatTypeMap.put(MutualFragment.class.toString(), mOptions.pages.get(Options.PAGE_MUTUAL).floatType);
            }
            if (mOptions.pages.containsKey(Options.PAGE_DIALOGS)) {
                mFloatTypeMap.put(DialogsFragment.class.toString(), mOptions.pages.get(Options.PAGE_DIALOGS).floatType);
            }
            if (mOptions.pages.containsKey(Options.PAGE_VISITORS)) {
                mFloatTypeMap.put(VisitorsFragment.class.toString(), mOptions.pages.get(Options.PAGE_VISITORS).floatType);
            }
            if (mOptions.pages.containsKey(Options.PAGE_BOOKMARKS)) {
                mFloatTypeMap.put(BookmarksFragment.class.toString(), mOptions.pages.get(Options.PAGE_BOOKMARKS).floatType);
            }
            if (mOptions.pages.containsKey(Options.PAGE_FANS)) {
                mFloatTypeMap.put(FansFragment.class.toString(), mOptions.pages.get(Options.PAGE_FANS).floatType);
            }
        }
    }

    public void onCreate() {
        if (mBanner != null) mBanner.onCreate();
    }

    public void onPause() {
        if (mBanner != null) mBanner.onPause();
        LocalBroadcastManager.getInstance(mFragment.getActivity())
                .unregisterReceiver(mOptionsUpdateReceiver);
    }

    public void onDestroy() {
        if (mBanner != null) mBanner.onDestroy();
    }

    public void onResume() {
        LocalBroadcastManager.getInstance(mFragment.getActivity()).registerReceiver(
                mOptionsUpdateReceiver,
                new IntentFilter(ProfileRequest.PROFILE_UPDATE_ACTION)
        );
        if (mLeaders != null) mLeaders.loadLeaders();
    }
}
