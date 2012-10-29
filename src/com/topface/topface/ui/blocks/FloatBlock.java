package com.topface.topface.ui.blocks;

import android.app.Activity;
import android.support.v4.app.DialogFragment;
import android.view.ViewGroup;
import com.topface.topface.data.Options;
import com.topface.topface.ui.fragments.TopsFragment;
import com.topface.topface.ui.fragments.feed.LikesFragment;
import com.topface.topface.ui.fragments.feed.MutualFragment;
import com.topface.topface.utils.CacheProfile;

import java.util.HashMap;
import java.util.Map;

/**
 * Блок для страниц, где нужно показывать баннеры или лидеров
 */
public class FloatBlock {
    private Map<String, String> mActivityMap;
    private Options mOptions;
    private Activity mActivity;
    private LeadersBlock mLeaders;
    private final ViewGroup mLayout;

    public FloatBlock(Activity activity, ViewGroup layoutView) {
        super();
        mOptions = CacheProfile.getOptions();
        mActivity = activity;
        mActivityMap = new HashMap<String, String>();
        mLayout = layoutView;
        setActivityMap();
        initBlock();
    }

    private void initBlock() {
        String currentActivity = mActivity.getClass().toString();
        if (mActivityMap.containsKey(currentActivity)) {
            String floatType = mActivityMap.get(currentActivity);
            if (floatType.equals(Options.FLOAT_TYPE_BANNER)) {
                new BannerBlock(mActivity, mLayout);
            } else if (floatType.equals(Options.FLOAT_TYPE_LEADERS)) {
                mLeaders = new LeadersBlock(mActivity, mLayout);
            }
        } else {
            //TODO: Просто тестируем
            mLeaders = new LeadersBlock(mActivity, mLayout);
        }

        //Если переданого активити нет в карте, то не инициализируем ни один блок
    }

    private void setActivityMap() {
        mActivityMap = new HashMap<String, String>();
        mActivityMap.put(LikesFragment.class.toString(), mOptions.float_type_like);
        mActivityMap.put(MutualFragment.class.toString(), mOptions.float_type_like);
        mActivityMap.put(TopsFragment.class.toString(), mOptions.float_type_top);
        mActivityMap.put(DialogFragment.class.toString(), mOptions.float_type_dialogs);
    }

    public void update() {
        if (mLeaders != null) {
            mLeaders.loadLeaders();
        }
    }
}
