package com.topface.topface.ui.blocks;

import android.app.Activity;
import android.support.v4.app.DialogFragment;
import com.topface.topface.data.Options;
import com.topface.topface.ui.fragments.LikesFragment;
import com.topface.topface.ui.fragments.MutualFragment;
import com.topface.topface.ui.fragments.TopsFragment;
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

    public FloatBlock(Activity activity) {
        super();
        mOptions = CacheProfile.getOptions();
        mActivity = activity;
        mActivityMap = new HashMap<String, String>();
        setActivityMap();
        initBlock();
    }

    private void initBlock() {
        String currentActivity = mActivity.getClass().toString();
        if (mActivityMap.containsKey(currentActivity)) {
            String floatType = mActivityMap.get(currentActivity);
            if (floatType.equals(Options.FLOAT_TYPE_BANNER)) {
                new BannerBlock(mActivity);
            }
            else if (floatType.equals(Options.FLOAT_TYPE_LEADERS)) {
                mLeaders = new LeadersBlock(mActivity);
            }
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
