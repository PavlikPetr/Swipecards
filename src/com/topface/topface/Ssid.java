package com.topface.topface;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

public class Ssid {
    public static final String PREFERENCES_SSID_KEY = "ssid";
    private static final String PREFERENCES_LAST_UPDATE_KEY = "ssid_last_update";
    // Data
    private static volatile String mSsid;
    private static Context mContext = App.getContext();
    private static long mLastUpdate;
    private static List<ISsidUpdateListener> updateListeners = new ArrayList<ISsidUpdateListener>();

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
        mSsid = preferences.getString(PREFERENCES_SSID_KEY, Static.EMPTY);
        mLastUpdate = preferences.getLong(PREFERENCES_LAST_UPDATE_KEY, 0);
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
        mLastUpdate = System.currentTimeMillis();
        new Thread(new Runnable() {
            @Override
            public void run() {
                SharedPreferences preferences = mContext.getSharedPreferences(Static.PREFERENCES_TAG_SHARED, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString(PREFERENCES_SSID_KEY, mSsid);
                editor.putLong(PREFERENCES_LAST_UPDATE_KEY, mLastUpdate);
                editor.commit();
            }
        }).start();
        
        for (ISsidUpdateListener listener : updateListeners) {
            listener.onUpdate();
        }
    }

    public synchronized static void remove() {
        mSsid = Static.EMPTY;
        new Thread(new Runnable() {
            @Override
            public void run() {
                SharedPreferences preferences = mContext.getSharedPreferences(Static.PREFERENCES_TAG_SHARED, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString(PREFERENCES_SSID_KEY, Static.EMPTY);
                editor.putLong(PREFERENCES_LAST_UPDATE_KEY, 0);
                editor.commit();
            }
        }).start();
    }

    /**
     * Определяет старше ли SSID указаного числа минут
     *
     * @param minutes время в минутах
     * @return старше ли SSID чем число минут передах в аргменте minutes
     */
    public synchronized static boolean isOlderThan(int minutes) {
        int millis = minutes * 60 * 1000;
        return System.currentTimeMillis() > (millis + mLastUpdate);
    }

    public synchronized static void addUpdateListener(ISsidUpdateListener listener) {
        updateListeners.add(listener);
    }

    public interface ISsidUpdateListener  {
        void onUpdate();
    }
}