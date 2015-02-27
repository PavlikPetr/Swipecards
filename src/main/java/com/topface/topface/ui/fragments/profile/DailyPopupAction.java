package com.topface.topface.ui.fragments.profile;

import android.content.Context;

import com.topface.topface.App;
import com.topface.topface.data.Options;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.controllers.AbstractStartAction;

/**
 * Базовый класс для попапов показывающихся периодически
 * Created by onikitin on 27.02.15.
 */
public abstract class DailyPopupAction extends AbstractStartAction {

    public Context mContext;
    private int mPriority;
    public Options mOptions;
    private String mTitle;

    public DailyPopupAction(Context context, int priority) {
        mContext = context;
        mPriority = priority;
        mTitle = this.getClass().getSimpleName();
        mOptions = CacheProfile.getOptions();
    }

    @Override
    public int getPriority() {
        return mPriority;
    }

    @Override
    public String getActionName() {
        return getTitle();
    }

    protected boolean isTimeoutEnded(long timeOut) {
        long lastTime = App.getUserConfig().getFacebookRequestWondowShow();
        if (lastTime == 0) {
            return firstStartShow();
        }
        long currentTime = System.currentTimeMillis();
        long deltaInSeconds = (currentTime - lastTime) / 1000;
        return (deltaInSeconds >= timeOut);
    }


    public String getTitle() {
        return mTitle;
    }

    protected abstract boolean firstStartShow();

}
