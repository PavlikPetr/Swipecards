package com.topface.topface.utils.config;

import android.content.Context;

/**
 * Created by kirussell on 11.01.14.
 * Contains all configs
 */
public class Configurations {

    private AppConfig mAppConfig;
    private ProfileConfig mProfileConfig;

    public Configurations(Context context) {
        mAppConfig = new AppConfig(context);
        mProfileConfig = new ProfileConfig(context);
    }

    public AppConfig getAppConfig() {
        return mAppConfig;
    }

    public ProfileConfig getProfileConfig() {
        return mProfileConfig;
    }

    public void onAuthTokenReceived() {
        mProfileConfig.onAuthTokenReceived();
    }
}
