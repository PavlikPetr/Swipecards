package com.topface.topface;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Point;
import com.topface.topface.data.City;
import com.topface.topface.data.search.SearchUser;
import com.topface.topface.utils.DateUtils;
import com.topface.topface.utils.Device;
import com.topface.topface.utils.Utils;

import java.util.LinkedList;

public class Data {
    // Data
    public static String SSID;
    public static LinkedList<City> cityList;
    public static int screen_width;
    public static LinkedList<SearchUser> searchList;
    public static int searchPosition = 0;

    public static void init(Context context) {
        //removeSSID(context); // for test
        loadSSID(context);

        //noinspection deprecation
        Point screenSize = Utils.getSrceenSize(context);
        screen_width = (Device.getOrientation(context) == Device.LANDSCAPE) ? screenSize.y : screenSize.x;

        DateUtils.syncTime();

        //LocalBroadcastManager.getInstance(context).registerReceiver(new ReAuthReceiver(), new IntentFilter(ReAuthReceiver.REAUTH_INTENT));

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