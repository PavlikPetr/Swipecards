package com.topface.topface.ui.blocks;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
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
    private Map<String, String> mBannersMap;
    private Options mOptions;
    private Activity mActivity;
    private Fragment mFragment;
    private LeadersBlock mLeaders;
    private BannerBlock mBanner;
    private final ViewGroup mLayout;

    public FloatBlock(Activity activity, Fragment fragment, ViewGroup layoutView) {
        super();
        mOptions = CacheProfile.getOptions();
        mActivity = activity;
        mFragment = fragment;
        mBannersMap = new HashMap<String, String>();
        mLayout = layoutView;
        setActivityMap();
        initBlock();
    }

    private void initBlock() {
        String currentFragment = mFragment.getClass().toString();
        if (mBannersMap.containsKey(currentFragment)) {
            String floatType = mBannersMap.get(currentFragment);
            if (floatType.equals(Options.FLOAT_TYPE_BANNER)) {
            	mBanner = new BannerBlock(mActivity,mFragment, mLayout);
            } else if (floatType.equals(Options.FLOAT_TYPE_LEADERS)) {
                mLeaders = new LeadersBlock(mActivity, mLayout);
            }
            mLeaders = new LeadersBlock(mActivity, mLayout);
        }
        //Если переданого активити нет в карте, то не инициализируем ни один блок
    }

    private void setActivityMap() {
        mBannersMap = new HashMap<String, String>();
        mBannersMap.put(LikesFragment.class.toString(), mOptions.float_type_like);
        mBannersMap.put(MutualFragment.class.toString(), mOptions.float_type_like);
        mBannersMap.put(TopsFragment.class.toString(), mOptions.float_type_top);
        mBannersMap.put(DialogsFragment.class.toString(), mOptions.float_type_dialogs);
        mBannersMap.put(VisitorsFragment.class.toString(), mOptions.float_type_like);
    }

    public void update() {
        if (mLeaders != null) {
            mLeaders.loadLeaders();
        }
    }
    
    public void onResume() {
    	if (mBanner != null) mBanner.onResume();
    	update();
    }
    
    public void onPause() {
    	if (mBanner != null) mBanner.onPause();
    }
    
    public boolean onKeyDown(int keyCode, KeyEvent event) {
    	if (mBanner != null) 
    		return mBanner.onKeyDown(keyCode, event);
    	return false;
    }
}
