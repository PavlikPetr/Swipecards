package com.topface.topface.utils.config;

import android.content.Context;

import com.topface.topface.utils.LocaleConfig;
import com.topface.topface.utils.Novice;
import com.topface.topface.utils.ads.BannersConfig;
import com.topface.topface.utils.social.AuthToken;

/**
 * Created by kirussell on 11.01.14.
 * Contains all configs and object that uses shared preferences
 */
public class Configurations {

    private Context mContext;

    private AppConfig mAppConfig;
    private UserConfig mUserConfig;
    private SessionConfig mSessionConfig;
    private BannersConfig mBannerConfig;
    private LocaleConfig mLocaleConfig;
    private Novice mNovice;


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
            ConfigConverter configConverter = new ConfigConverter(AuthToken.getInstance().getUserTokenUniqueId());
            if (configConverter.hasOldConfig()) {
                configConverter.divConfig();
                configConverter.removeOldConfig();
                mUserConfig = configConverter.getMainUserConfig();
            } else {
                mUserConfig = new UserConfig(mContext);
            }
        }
        return mUserConfig;
    }

    public UserConfig rebuildUserConfig(String oldEmail) {
        ConfigConverter manager = new ConfigConverter();
        manager.rebuildConfig(oldEmail, AuthToken.getInstance().getUserTokenUniqueId());
        mUserConfig.saveConfig();
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

    public Novice getNovice() {
        if (mNovice == null) {
            mNovice = new Novice();
        }
        return mNovice;
    }

    public void onAuthTokenReceived() {
        getUserConfig().onAuthTokenReceived();
    }

    public void onLogout() {
        getSessionConfig().resetAndSaveConfig();
        mNovice = null;
    }
}
