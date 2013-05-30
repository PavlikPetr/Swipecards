package com.topface.topface.utils;

import android.content.*;
import android.content.res.Resources;
import com.topface.topface.App;

import java.util.Locale;

public class LocaleConfig {

    private static final String SYSTEM_LOCALE = "com.topface.topface_system_locale";
    private static final String APPLICATION_LOCALE = "com.topface.topface_application_locale";

    private Context mContext;
    private String mSystemLocale;
    private String mApplicationLocale;

    public LocaleConfig(Context context) {
        mContext = context;
    }

    private void fetchToSystemLocale() {
        Locale currentSystemLocale = new Locale(Locale.getDefault().getLanguage());
        Locale savedSystemLocale = new Locale(getSystemLocale());
        if (!savedSystemLocale.equals(currentSystemLocale)) {
            setSystemLocale(currentSystemLocale.getLanguage());
            setApplicationLocale(currentSystemLocale.getLanguage());
        }
    }

    public static void updateConfiguration(Context baseContext) {
        Resources res = baseContext.getResources();
        android.content.res.Configuration conf = res.getConfiguration();
        conf.locale = new Locale(App.getConfig().getLocaleConfig().getApplicationLocale());
        res.updateConfiguration(conf, res.getDisplayMetrics());
    }

    public String getSystemLocale() {
        if (mSystemLocale == null) {
            mSystemLocale = getPreferences().getString(SYSTEM_LOCALE, Locale.getDefault().getLanguage());
        }
        return mSystemLocale;
    }

    public String getApplicationLocale() {
        fetchToSystemLocale();
        if (mApplicationLocale == null) {
            mApplicationLocale = getPreferences().getString(APPLICATION_LOCALE,Locale.getDefault().getLanguage());
        }
        return mApplicationLocale;
    }

    public boolean setSystemLocale(String locale) {
        mSystemLocale = locale;
        return getPreferences().edit().putString(SYSTEM_LOCALE,mSystemLocale).commit();
    }

    public boolean setApplicationLocale(String locale) {
        mApplicationLocale = locale;
        setSystemLocale(Locale.getDefault().getLanguage());
        return getPreferences().edit().putString(APPLICATION_LOCALE,mApplicationLocale).commit();
    }

    private SharedPreferences getPreferences() {
        return mContext.getSharedPreferences(
                AppConfig.BASE_CONFIG_SETTINGS,
                Context.MODE_PRIVATE
        );
    }
}
