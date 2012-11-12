package com.topface.topface.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.data.Options;
import com.topface.topface.receivers.ConnectionChangeReceiver;
import com.topface.topface.requests.SendMailNotificationsRequest;
import com.topface.topface.utils.social.AuthorizationManager;

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

    public static final String SETTINGS_SOCIAL_ACCOUNT_NAME = "social_account_name";


    private SharedPreferences mSettings;
    private SharedPreferences.Editor mEditor;
    private Context mContext;

    private Settings() {
        mContext = App.getContext();
        mSettings = PreferenceManager.getDefaultSharedPreferences(App.getContext());
        mEditor = mSettings.edit();
    }

    public static Settings getInstance() {
        if (mInstance == null) {
            mInstance = new Settings();
        }

        return mInstance;
    }

    public void setSetting(String key, boolean value) {
        mEditor.putBoolean(key, value);
        mEditor.commit();
    }

    public void setSocialAccountName(String name) {
        mEditor.putString(SETTINGS_SOCIAL_ACCOUNT_NAME, name);
        mEditor.commit();
    }

    public boolean getSetting(String key) {
        return mSettings.getBoolean(key, true);
    }

    public String getSocialAccountName() {
        return mSettings.getString(SETTINGS_SOCIAL_ACCOUNT_NAME, Static.EMPTY);
    }

    public void getSocialAccountNameAsync(final Handler handler) {
        (new Thread() {
            @Override
            public void run() {
                AuthorizationManager.getAccountName(handler);
            }
        }).start();
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

    public void resetSettings() {
        setSocialAccountName(Static.EMPTY);
    }
        
    public SendMailNotificationsRequest getMailNotificationRequest(int key, boolean isMail, boolean value, Context context) {
        SendMailNotificationsRequest request = new SendMailNotificationsRequest(context);
        Options options = CacheProfile.getOptions();


        request.mailsympathy = options.notifications.get(Options.NOTIFICATIONS_LIKES).mail;
        request.mailmutual = options.notifications.get(Options.NOTIFICATIONS_SYMPATHY).mail;
        request.mailchat = options.notifications.get(Options.NOTIFICATIONS_MESSAGE).mail;
        request.mailguests = options.notifications.get(Options.NOTIFICATIONS_VISITOR).mail;

        request.apnssympathy = options.notifications.get(Options.NOTIFICATIONS_LIKES).apns;
        request.apnsmutual = options.notifications.get(Options.NOTIFICATIONS_SYMPATHY).apns;
        request.apnschat = options.notifications.get(Options.NOTIFICATIONS_MESSAGE).apns;
        request.apnsguests = options.notifications.get(Options.NOTIFICATIONS_VISITOR).apns;

        switch (key) {
            case Options.NOTIFICATIONS_LIKES:
                if(isMail) request.mailsympathy = value;
                else request.apnssympathy = value;
                break;
            case Options.NOTIFICATIONS_MESSAGE:
                if(isMail) request.mailchat = value;
                else request.apnschat = value;
                break;
            case Options.NOTIFICATIONS_SYMPATHY:
                if(isMail) request.mailmutual = value;
                else request.apnsmutual = value;
                break;
            case Options.NOTIFICATIONS_VISITOR:
                if(isMail) request.mailguests = value;
                else request.apnsguests = value;
                break;
            default:
                return null;

        }

        return request;
    }
}
