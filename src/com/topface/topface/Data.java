package com.topface.topface;

import android.content.Context;
import android.content.SharedPreferences;
import com.facebook.android.Facebook;
import com.topface.topface.data.Album;
import com.topface.topface.data.City;
import com.topface.topface.utils.Device;

import java.util.LinkedList;

public class Data {
    // Data
    public static String SSID;
    public static Facebook facebook;
    public static LinkedList<City> cityList;
    public static LinkedList<Album> photoAlbum;
    public static int GRID_COLUMN;
    public static int screen_width;

    public static void init(Context context) {
        //removeSSID(context);
        loadSSID(context);
        facebook = new Facebook(Static.AUTH_FACEBOOK_ID);

        screen_width = (Device.getOrientation(context) == Device.LANDSCAPE)
                ? Device.getDisplay(context).getHeight()
                : Device.getDisplay(context).getWidth();

        switch (screen_width) {
            case Device.W_240:
            case Device.W_320:
                GRID_COLUMN = 2;
                break;
            case Device.W_480:
                GRID_COLUMN = 3;
                break;
            default:
                GRID_COLUMN = 4;
                break;
        }
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
