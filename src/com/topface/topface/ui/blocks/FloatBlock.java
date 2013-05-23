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
    private static Map<String, Options.Page> mBannersMap;
    private Fragment mFragment;
    private BannerBlock mBanner;
    private final ViewGroup mLayout;
    private LeadersBlock mLeaders;
    private BroadcastReceiver mOptionsUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mBannersMap = null;
            getActivityMap();
        }
    };

    public FloatBlock(Fragment fragment, ViewGroup layoutView) {
        super();
        mFragment = fragment;
        mLayout = layoutView;
        getActivityMap();
        initBlock();
    }

    private void initBlock() {
        String currentFragment = mFragment.getClass().toString();
        if (mBannersMap.containsKey(currentFragment)) {
            String floatType = mBannersMap.get(currentFragment).floatType;
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

    public static Map<String,Options.Page> getActivityMap() {
        if (mBannersMap == null) {
            mBannersMap = new HashMap<String, Options.Page>();
            Options mOptions = CacheProfile.getOptions();
            if (mOptions.pages.containsKey(Options.PAGE_LIKES)) {
                mBannersMap.put(LikesFragment.class.toString(), mOptions.pages.get(Options.PAGE_LIKES));
            }
            if (mOptions.pages.containsKey(Options.PAGE_MUTUAL)) {
                mBannersMap.put(MutualFragment.class.toString(), mOptions.pages.get(Options.PAGE_MUTUAL));
            }
            if (mOptions.pages.containsKey(Options.PAGE_DIALOGS)) {
                mBannersMap.put(DialogsFragment.class.toString(), mOptions.pages.get(Options.PAGE_DIALOGS));
            }
            if (mOptions.pages.containsKey(Options.PAGE_VISITORS)) {
                mBannersMap.put(VisitorsFragment.class.toString(), mOptions.pages.get(Options.PAGE_VISITORS));
            }
            if (mOptions.pages.containsKey(Options.PAGE_BOOKMARKS)) {
                mBannersMap.put(BookmarksFragment.class.toString(), mOptions.pages.get(Options.PAGE_BOOKMARKS));
            }
            if (mOptions.pages.containsKey(Options.PAGE_FANS)) {
                mBannersMap.put(FansFragment.class.toString(), mOptions.pages.get(Options.PAGE_FANS));
            }
        }
        return mBannersMap;
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
