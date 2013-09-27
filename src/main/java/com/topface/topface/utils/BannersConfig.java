package com.topface.topface.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.support.v4.content.LocalBroadcastManager;

import com.topface.topface.Static;
import com.topface.topface.data.Options;
import com.topface.topface.requests.ProfileRequest;
import com.topface.topface.ui.blocks.FloatBlock;

import java.util.HashMap;

/**
 * Аналогично AppConfig.java, для настроек баннеров
 * (Класс для хранения в SharedPreferences тех настроек, которые обязательны для работы приложения,
 * но которые можно изменить в рантайме. Нужно прежде всего для редакторских функций)
 */
public class BannersConfig {

    public static final String BANNERS_CONFIG_SETTINGS = "banners_config_settings";
    private static final String BANNERS_CONFIG_ON_START = "banners_config_settings_on_start";

    private final Context mContext;

    public BannersConfig(Context context) {
        mContext = context;
        initSavedOptionsPages();
        LocalBroadcastManager.getInstance(context).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                initSavedOptionsPages();
            }
        }, new IntentFilter(ProfileRequest.PROFILE_UPDATE_ACTION));
    }

    public void initSavedOptionsPages() {
        if (needLoadOnStart()) {
            restoreBannersSettings();
        }
    }

    public boolean needLoadOnStart() {
        return getPreferences().getBoolean(BANNERS_CONFIG_ON_START,false);
    }

    public void saveBannersSettings() {
        SharedPreferences preferences = getPreferences();
        SharedPreferences.Editor editor = preferences.edit();
        Options options = CacheProfile.getOptions();
        for (String pageName : options.pages.keySet()) {
            editor.putString(pageName, options.pages.get(pageName).toString());
        }
        editor.commit();
    }

    public void restoreBannersSettings() {
        SharedPreferences preferences = getPreferences();
        Options options = CacheProfile.getOptions();
        options.pages = new HashMap<String, Options.Page>();
        for (String pageName : Options.PAGES) {
            String str = preferences.getString(pageName, Static.EMPTY);
            if (!str.isEmpty()) {
                options.pages.put(pageName,Options.Page.parseFromString(str));
            }
        }
    }

    public void resetBannersSettings() {
        CacheProfile.clearOptions();
        FloatBlock.resetActivityMap();
        getPreferences().edit().clear().commit();
    }

    public void setLoadOnStart(boolean start) {
         getPreferences().edit().putBoolean(BANNERS_CONFIG_ON_START,start).commit();
    }

    private SharedPreferences getPreferences() {
        return mContext.getSharedPreferences(
                BANNERS_CONFIG_SETTINGS,
                Context.MODE_PRIVATE
        );
    }
}
