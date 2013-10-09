package com.topface.topface.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.widget.TextView;

import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.requests.SendMailNotificationsRequest;
import com.topface.topface.utils.social.AuthToken;
import com.topface.topface.utils.social.AuthorizationManager;

/**
 * Вспомогательный класс для работы с настройками приложения
 */
public class Settings {
    public static final String SILENT = "silent";
    private static Settings mInstance;
    public static final String SETTINGS_C2DM_RINGTONE = "settings_c2dm_ringtone";
    public static final String NOTIFICATION_MELODY = "notification_melody";
    public static final String SETTINGS_C2DM_VIBRATION = "settings_c2dm_vibration";
    public static final String SETTINGS_C2DM = "settings_c2dm";
    public static final String DEFAULT_SOUND = "DEFAULT_SOUND";

    public static final String SETTINGS_SOCIAL_ACCOUNT_NAME = "social_account_name";
    public static final String SETTINGS_SOCIAL_ACCOUNT_EMAIL = "social_account_email";

    public static final int REQUEST_CODE_RINGTONE = 333;

    private SharedPreferences mSettings;
    private SharedPreferences.Editor mEditor;

    private Settings() {
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

    public void setSetting(String key, String value) {
        mEditor.putString(key, value);
        mEditor.commit();
    }

    public void setSocialAccountName(String name) {
        mEditor.putString(SETTINGS_SOCIAL_ACCOUNT_NAME, name);
        mEditor.commit();
    }

    public void setSocialAccountEmail(String email) {
        mEditor.putString(SETTINGS_SOCIAL_ACCOUNT_EMAIL, email);
        mEditor.commit();
    }

    public String getSocialAccountName() {
        return mSettings.getString(SETTINGS_SOCIAL_ACCOUNT_NAME, Static.EMPTY);
    }

    public String getRingtoneName() {
        return mSettings.getString(NOTIFICATION_MELODY, App.getContext().getString(R.string.silent_ringtone));
    }

    public void getSocialAccountName(final TextView textView) {
        AuthToken authToken = AuthToken.getInstance();
        if (authToken.getSocialNet().equals(AuthToken.SN_TOPFACE)) {
            textView.setText(authToken.getLogin());
        } else {
            String name = getSocialAccountName();
            if (TextUtils.isEmpty(name)) {
                getSocialAccountNameAsync(new Handler(){
                    @Override
                    public void handleMessage(Message msg) {
                        final String socialName = (String) msg.obj;
                        textView.post(new Runnable() {

                            @Override
                            public void run() {
                                textView.setText(socialName);
                            }
                        });
                        setSocialAccountName(socialName);
                    }
                });
            } else {
                textView.setText(name);
            }
        }
    }

    public void getSocialAccountIcon(final TextView textView) {
        AuthToken authToken = AuthToken.getInstance();
        if (authToken.getSocialNet().equals(AuthToken.SN_FACEBOOK)) {
            textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_fb, 0, 0, 0);
        } else if (authToken.getSocialNet().equals(AuthToken.SN_VKONTAKTE)) {
            textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_vk, 0, 0, 0);
        } else if (authToken.getSocialNet().equals(AuthToken.SN_TOPFACE)) {
            textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_tf, 0, 0, 0);
        } else if (authToken.getSocialNet().equals(AuthToken.SN_ODNOKLASSNIKI)) {
            textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ico_ok_settings, 0, 0, 0);
        }
    }

    public String getSocialAccountEmail() {
        return mSettings.getString(SETTINGS_SOCIAL_ACCOUNT_EMAIL, Static.EMPTY);
    }

    public void getSocialAccountNameAsync(final Handler handler) {
        (new Thread() {
            @Override
            public void run() {
                AuthorizationManager.getAccountName(handler);
            }
        }).start();
    }

    public Uri getRingtone() {
        if (mSettings.getString(SETTINGS_C2DM_RINGTONE, DEFAULT_SOUND).equals(SILENT)) {
            return null;
        }
        String ringtone = mSettings.getString(SETTINGS_C2DM_RINGTONE, DEFAULT_SOUND);
        return ringtone.equals(DEFAULT_SOUND) ? RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION) : Uri.parse(ringtone);
    }

    public Boolean isVibrationEnabled() {
        return mSettings.getBoolean(SETTINGS_C2DM_VIBRATION, true);
    }

    public boolean isNotificationEnabled() {
        return mSettings.getBoolean(SETTINGS_C2DM, true);
    }

    public void resetSettings() {
        setSocialAccountName(Static.EMPTY);
        setSocialAccountEmail(Static.EMPTY);
    }

    public SendMailNotificationsRequest getMailNotificationRequest(int key, boolean isMail, boolean value, Context context) {
        SendMailNotificationsRequest request = getMailNotificationRequest(context);

        switch (key) {
            case CacheProfile.NOTIFICATIONS_LIKES:
                if (isMail) request.mailSympathy = value;
                else request.apnsSympathy = value;
                break;
            case CacheProfile.NOTIFICATIONS_MESSAGE:
                if (isMail) request.mailChat = value;
                else request.apnsChat = value;
                break;
            case CacheProfile.NOTIFICATIONS_SYMPATHY:
                if (isMail) request.mailMutual = value;
                else request.apnsMutual = value;
                break;
            case CacheProfile.NOTIFICATIONS_VISITOR:
                if (isMail) request.mailGuests = value;
                else request.apnsVisitors = value;
                break;
            default:
                return null;
        }

        return request;
    }

    public SendMailNotificationsRequest getMailNotificationRequest(Context context) {
        SendMailNotificationsRequest request = new SendMailNotificationsRequest(context);
        if (CacheProfile.notifications != null) {
            try {
                request.mailSympathy = CacheProfile.notifications.get(CacheProfile.NOTIFICATIONS_LIKES).mail;
                request.mailMutual = CacheProfile.notifications.get(CacheProfile.NOTIFICATIONS_SYMPATHY).mail;
                request.mailChat = CacheProfile.notifications.get(CacheProfile.NOTIFICATIONS_MESSAGE).mail;
                request.mailGuests = CacheProfile.notifications.get(CacheProfile.NOTIFICATIONS_VISITOR).mail;
            } catch (Exception e) {
                Debug.error(e);
            }

            try {
                request.apnsSympathy = CacheProfile.notifications.get(CacheProfile.NOTIFICATIONS_LIKES).apns;
                request.apnsMutual = CacheProfile.notifications.get(CacheProfile.NOTIFICATIONS_SYMPATHY).apns;
                request.apnsChat = CacheProfile.notifications.get(CacheProfile.NOTIFICATIONS_MESSAGE).apns;
                request.apnsVisitors = CacheProfile.notifications.get(CacheProfile.NOTIFICATIONS_VISITOR).apns;
            } catch (Exception e) {
                Debug.error(e);
            }
        }
        return request;
    }
}
