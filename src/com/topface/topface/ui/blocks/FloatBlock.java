package com.topface.topface.ui.blocks;

import android.app.Activity;
import com.topface.topface.data.Options;
import com.topface.topface.ui.*;
import com.topface.topface.utils.CacheProfile;

import java.util.HashMap;
import java.util.Map;

/**
 * Блок для страниц, где нужно показывать баннеры или лидеров
 */
public class FloatBlock {
    private Map<String, String> mActivityMap = new HashMap<String, String>();
    private Options mOptions;
    private Activity mActivity;
    private LeadersBlock mLeaders;

    public FloatBlock(Activity activity) {
        super();
        mOptions = CacheProfile.getOptions();
        mActivity = activity;
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
        mActivityMap.put(LikesActivity.class.toString(), mOptions.float_type_like);
        mActivityMap.put(SymphatyActivity.class.toString(), mOptions.float_type_like);
        mActivityMap.put(TopsActivity.class.toString(), mOptions.float_type_top);
        mActivityMap.put(InboxActivity.class.toString(), mOptions.float_type_dialogs);
    }

    public void update() {
        if (mLeaders != null) {
            mLeaders.loadLeaders();
        }
    }
}
