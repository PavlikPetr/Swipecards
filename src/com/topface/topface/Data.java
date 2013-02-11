package com.topface.topface;

import android.content.Context;
import android.content.SharedPreferences;
import com.topface.topface.data.City;
import com.topface.topface.data.search.SearchUser;
import com.topface.topface.utils.DateUtils;

import java.util.LinkedList;

public class Data {
    // Data
    public static String SSID;
    public static LinkedList<City> cityList;
    public static LinkedList<SearchUser> searchList;
    public static int searchPosition = 0;

    public static void init(Context context) {
        loadSSID(context);
        DateUtils.syncTime();
    }

    public static boolean isSSID() {
        return SSID != null && SSID.length() > 0;
    }

    public static String loadSSID(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(Static.PREFERENCES_TAG_SHARED, Context.MODE_PRIVATE);
        SSID = preferences.getString(Static.PREFERENCES_SSID, Static.EMPTY);
        return SSID;
    }

    public static void saveSSID(Context context, String ssid) {
        SSID = (ssid == null || ssid.length() == 0) ? Static.EMPTY : ssid;
        SharedPreferences preferences = context.getSharedPreferences(Static.PREFERENCES_TAG_SHARED, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(Static.PREFERENCES_SSID, SSID);
        editor.commit();
    }

    public static void removeSSID(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(Static.PREFERENCES_TAG_SHARED, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(Static.PREFERENCES_SSID, SSID = Static.EMPTY);
        editor.commit();
    }
}