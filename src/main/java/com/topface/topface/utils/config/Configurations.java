package com.topface.topface.utils.config;

import android.content.Context;

import com.topface.topface.utils.BannersConfig;
import com.topface.topface.utils.LocaleConfig;

/**
 * Created by kirussell on 11.01.14.
 * Contains all configs
 */
public class Configurations {

    private Context mContext;

    private AppConfig mAppConfig;
    private UserConfig mUserConfig;
    private SessionConfig mSessionConfig;
    private BannersConfig mBannerConfig;
    private LocaleConfig mLocaleConfig;


    public Configurations(Context context) {
        mContext = context;
    }

    public AppConfig getAppConfig() {
        if (mAppConfig == null) {
            mAppConfig = new AppConfig(mContext);
        }
        return mAppConfig;
    }

    public UserConfig getUserConfig() {
        if (mUserConfig == null) {
            mUserConfig = new UserConfig(mContext);
        }
        return mUserConfig;
    }

    public SessionConfig getSessionConfig() {
        if (mSessionConfig == null) {
            mSessionConfig = new SessionConfig(mContext);
        }
        return mSessionConfig;
    }

    public BannersConfig getBannerConfig() {
        if (mBannerConfig == null) {
            mBannerConfig = new BannersConfig(mContext);
        }
        return mBannerConfig;
    }

    public LocaleConfig getLocaleConfig() {
        if (mLocaleConfig == null) {
            mLocaleConfig = new LocaleConfig(mContext);
        }
        return mLocaleConfig;
    }

    public void onAuthTokenReceived() {
        getUserConfig().onAuthTokenReceived();
    }

    public void onLogout() {
        getSessionConfig().resetAndSaveConfig();
    }
}
