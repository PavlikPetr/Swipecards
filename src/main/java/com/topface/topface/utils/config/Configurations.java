package com.topface.topface.utils.config;

import android.content.Context;

import com.topface.framework.utils.Debug;
import com.topface.topface.App;
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
    private UserConfigConverter mConfigConverter;

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
            if (App.getAppConfig().isUserConfigConverted()) {
                if (UserConfigConverter.hasOldConfig()) {
                    //если у пользователя старый конфиг, то конвертируем его в новые
                    mConfigConverter = new UserConfigConverter(AuthToken.getInstance().getUserTokenUniqueId(), new UserConfigConverter.OnUpdateUserConfig() {
                        @Override
                        public void onUpdate() {
                            mUserConfig = mConfigConverter.getMainUserConfig();
                            Debug.debug(mConfigConverter, "Config converting complite " + mUserConfig.getSettingsMap().size());
                        }
                    });
                    Debug.debug(mConfigConverter, "Converting old config");
                    mConfigConverter.convertConfig();
                }

            } else {
                if (!(mConfigConverter != null &&
                        mConfigConverter.getConverterState() != UserConfigConverter.ConverterState.DEFAULT)) {
                    Debug.debug(mConfigConverter, "Create new config");
                    mUserConfig = new UserConfig(mContext);
                }
            }
        }
        return mUserConfig;
    }

    public UserConfig rebuildUserConfig(String oldEmail) {
        UserConfigConverter configConverter = new UserConfigConverter();
        configConverter.rebuildConfig(oldEmail, AuthToken.getInstance().getUserTokenUniqueId());
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
