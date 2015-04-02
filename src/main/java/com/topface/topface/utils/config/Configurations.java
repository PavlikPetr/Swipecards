package com.topface.topface.utils.config;

import android.content.Context;
import android.text.TextUtils;

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


    /**
     * Возвращет конфиг текущего пользователя, если конфига нет создает новый. Если есть старый
     * общий конфиг запустит процеес деления конфига. Если пользователь перелогинился , то создастся
     * новый конфиг и заполниться данными из конфига, если конфига нет то создается новый пустой.
     * <p/>
     * Не вызывать метод до логина пользователся, иначе создастся файл конфига с неверным именем
     * (т.к. токен будет пуст) и могут потеряться данные.
     *
     * @return config for current user
     */
    public UserConfig getUserConfig() {
        if (mUserConfig == null) {
            if (App.getAppConfig().isNeedConverting() && UserConfigConverter.hasOldConfig()) {
                    //если у пользователя старый конфиг, то конвертируем его в новые
                mConfigConverter = new UserConfigConverter(AuthToken.getInstance().getUserTokenUniqueId(), new UserConfigConverter.OnUpdateUserConfig() {
                    @Override
                    public void onUpdate() {
                        mUserConfig = mConfigConverter.getMainUserConfig();
                        Debug.debug(mConfigConverter, "Config1 converting complite ");
                    }
                });
                Debug.debug(mConfigConverter, "Converting old config1");
                mConfigConverter.convertConfig();
                mUserConfig = new TempUserConfig(mContext);
            } else {
                if (!(mConfigConverter != null &&
                        mConfigConverter.getConverterState() != UserConfigConverter.ConverterState.DEFAULT)) {
                    Debug.debug(mConfigConverter, "Create new config1");
                    mUserConfig = new UserConfig(null, mContext);
                }
            }
        } else {
            if (!TextUtils.isEmpty(AuthToken.getInstance().getUserTokenUniqueId()) &&
                    !AuthToken.getInstance().getUserTokenUniqueId().equals(mUserConfig.getUnique())) {
                Debug.debug(mConfigConverter, "Create new config1 after equals");
                mUserConfig = new UserConfig(AuthToken.getInstance().getUserTokenUniqueId(), mContext);
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
