package com.topface.topface.utils.controllers.startactions;

import android.content.Context;

import com.topface.topface.App;
import com.topface.topface.data.Options;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.config.UserConfig;

/**
 * Базовый класс для попапов показывающихся через определенный промежуток времени
 * Created by onikitin on 27.02.15.
 */
public abstract class DailyPopupAction implements IStartAction {

    private Context mContext;
    private Options mOptions;
    private UserConfig mUserConfig;

    public DailyPopupAction(Context context) {
        mContext = context;
        mOptions = CacheProfile.getOptions();
        mUserConfig = App.getUserConfig();
    }

    protected boolean isTimeoutEnded(long timeOut, long lastTime) {
        if (lastTime == 0) {
            return firstStartShow();
        }
        long currentTime = System.currentTimeMillis();
        long deltaInSeconds = (currentTime - lastTime) / 1000;
        return (deltaInSeconds >= timeOut);
    }

    protected Context getContext(){
        return mContext;
    }

    protected Options getOptions() {
        return mOptions;
    }

    protected UserConfig getUserConfig() {
        return mUserConfig;
    }



    protected abstract boolean firstStartShow();

}
