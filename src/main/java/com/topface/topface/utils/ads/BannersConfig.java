package com.topface.topface.utils.ads;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;

import com.topface.topface.Static;
import com.topface.topface.banners.PageInfo;
import com.topface.topface.data.Options;
import com.topface.topface.utils.CacheProfile;

import java.util.HashMap;
import java.util.Map;

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
        }, new IntentFilter(CacheProfile.PROFILE_UPDATE_ACTION));
    }

    public void initSavedOptionsPages() {
        if (needLoadOnStart()) {
            restoreBannersSettings();
        }
    }

    public boolean needLoadOnStart() {
        return getPreferences().getBoolean(BANNERS_CONFIG_ON_START, false);
    }

    public void saveBannersSettings() {
        SharedPreferences preferences = getPreferences();
        SharedPreferences.Editor editor = preferences.edit();
        Options options = CacheProfile.getOptions();
        Map<String, PageInfo> pagesInfo = options.getPagesInfo();
        for (String pageName : pagesInfo.keySet()) {
            editor.putString(pageName, pagesInfo.get(pageName).toString());
        }
        editor.apply();
    }

    public void restoreBannersSettings() {
        SharedPreferences preferences = getPreferences();
        Map<String, PageInfo> pagesInfo= new HashMap<>();
        for (PageInfo.PageName pageName : PageInfo.PageName.values()) {
            String str = preferences.getString(pageName.getName(), Static.EMPTY);
            if (!TextUtils.isEmpty(str)) {
                pagesInfo.put(pageName.getName(), PageInfo.parseFromString(str));
            }
        }
        CacheProfile.getOptions().setPagesInfo(pagesInfo);
    }

    public void resetBannersSettings() {
        CacheProfile.clearOptions();
        getPreferences().edit().clear().commit();
    }

    public void setLoadOnStart(boolean start) {
        getPreferences().edit().putBoolean(BANNERS_CONFIG_ON_START, start).commit();
    }

    private SharedPreferences getPreferences() {
        return mContext.getSharedPreferences(
                BANNERS_CONFIG_SETTINGS,
                Context.MODE_PRIVATE
        );
    }
}
