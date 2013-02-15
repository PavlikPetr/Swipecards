package com.topface.topface;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

public class Ssid {
    // Data
    private static volatile String mSsid;
    private static Context mContext = App.getContext();

    public static void init() {
        load();
    }

    public static boolean isLoaded() {
        return !TextUtils.isEmpty(mSsid);
    }

    public static boolean isEmpty() {
        return TextUtils.isEmpty(get());
    }

    public synchronized static String load() {
        SharedPreferences preferences = mContext.getSharedPreferences(Static.PREFERENCES_TAG_SHARED, Context.MODE_PRIVATE);
        mSsid = preferences.getString(Static.PREFERENCES_SSID, Static.EMPTY);
        return mSsid;
    }

    public synchronized static String get() {
        if (mSsid == null) {
            load();
        }

        return mSsid;
    }

    public synchronized static void save(String ssid) {
        mSsid = TextUtils.isEmpty(ssid) ? Static.EMPTY : ssid;
        SharedPreferences preferences = mContext.getSharedPreferences(Static.PREFERENCES_TAG_SHARED, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(Static.PREFERENCES_SSID, mSsid);
        editor.commit();
    }

    public synchronized static void remove() {
        mSsid = Static.EMPTY;
        SharedPreferences preferences = mContext.getSharedPreferences(Static.PREFERENCES_TAG_SHARED, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(Static.PREFERENCES_SSID, Static.EMPTY);
        editor.commit();
    }
}