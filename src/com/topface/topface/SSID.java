package com.topface.topface;

import android.content.Context;
import android.content.SharedPreferences;

public class Ssid {
    // Data
    public static String SSID;

    public static void init(Context context) {
        load(context);
    }

    public static boolean isLoaded() {
        return SSID != null && SSID.length() > 0;
    }

    public static String load(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(Static.PREFERENCES_TAG_SHARED, Context.MODE_PRIVATE);
        SSID = preferences.getString(Static.PREFERENCES_SSID, Static.EMPTY);
        return SSID;
    }

    public static void save(Context context, String ssid) {
        SSID = (ssid == null || ssid.length() == 0) ? Static.EMPTY : ssid;
        SharedPreferences preferences = context.getSharedPreferences(Static.PREFERENCES_TAG_SHARED, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(Static.PREFERENCES_SSID, SSID);
        editor.commit();
    }

    public static void remove(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(Static.PREFERENCES_TAG_SHARED, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(Static.PREFERENCES_SSID, SSID = Static.EMPTY);
        editor.commit();
    }
}