package com.topface.topface.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.receivers.ConnectionChangeReceiver;

/**
 * Вспомогательный класс для работы с настройками приложения
 */
public class Settings {
    private static Settings mInstance;
    public static final String SETTINGS_PRELOADING = "settings_preloading";
    //public static final String SETTINGS_PRELOADING_TYPE = "settings_preloading_type";
    public static final String SETTINGS_C2DM_RINGTONE = "settings_c2dm_ringtone";
    public static final String SETTINGS_C2DM_VIBRATION = "settings_c2dm_vibration";
    public static final String SETTINGS_C2DM = "settings_c2dm";
    public static final String DEFAULT_SOUND = "DEFAULT_SOUND";
    private SharedPreferences mSettings;
    private Context mContext;

    private Settings() {
        mContext = App.getContext();
        mSettings = PreferenceManager.getDefaultSharedPreferences(App.getContext());
    }

    public static Settings getInstance() {
        if (mInstance == null) {
            mInstance = new Settings();
        }

        return mInstance;
    }

    public String getPreloading() {
        return mSettings.getString(SETTINGS_PRELOADING, mContext.getString(R.string.settings_preloading_wifi));
    }

    public String getPreloadingType() {
        //Пока предзагрузка пользователей не поддерживается
        //return mSettings.getString(SETTINGS_PRELOADING_TYPE, mContext.getString(R.string.settings_preloading_type_user));
        return mContext.getString(R.string.settings_preloading_type_photo);
    }

    public String getRingtone() {
        return mSettings.getString(SETTINGS_C2DM_RINGTONE, DEFAULT_SOUND);
    }

    public Boolean isVibrationEnabled() {
        return mSettings.getBoolean(SETTINGS_C2DM_VIBRATION, true);
    }

    public boolean isNotificationEnabled() {
        return mSettings.getBoolean(SETTINGS_C2DM, true);
    }

    /*public boolean isPreloadingDisabled() {
        return getPreloading().equals(mContext.getString(R.string.settings_preloading_off));
    }*/

    /**
     * Нужно ли в данный момент запускать предзагрузку следующей фотографии альбома
     *
     * @return начинать ли предзагрузку
     */
    public boolean isPreloadPhoto() {
        return isPreloadingEnabled(mContext.getString(R.string.settings_preloading_type_photo));
    }

    /**
     * Нужно ли в данный момент запускать предзагрузку фотографии следующего пользователя
     *
     * @return начинать ли предзагрузку
     */
    @SuppressWarnings("UnusedDeclaration")
    public boolean isPreloadUser() {
        return isPreloadingEnabled(mContext.getString(R.string.settings_preloading_type_user));
    }

    private boolean isPreloadingEnabled(String type) {
        String preloadingValue = getPreloading();
        String preloadingType = getPreloadingType();
        //Проверяем тип предзагрузки
        if (preloadingType.equals(mContext.getString(R.string.settings_preloading_type_all)) ||
                preloadingType.equals(type)) {

            //Если верный, то смотрим, включена ли она
            if (preloadingValue.equals(mContext.getString(R.string.settings_preloading_on))) {
                return true;
            } else if (preloadingValue.equals(mContext.getString(R.string.settings_preloading_wifi))) {
                //Если включена только по wifi, то проверяем состояние соединения
                return !ConnectionChangeReceiver.isMobileConnection();
            }

        }

        return false;
    }
}
