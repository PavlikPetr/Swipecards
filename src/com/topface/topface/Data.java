package com.topface.topface;

import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.support.v4.content.LocalBroadcastManager;
import android.util.SparseArray;
import com.facebook.android.Facebook;
import com.topface.topface.data.*;
import com.topface.topface.utils.Device;

import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;

public class Data {
    // Data
    public static String SSID;
    public static Bitmap friendAvatar;
    public static Bitmap ownerAvatar;
    public static Facebook facebook;
    public static LinkedList<City> cityList;
    public static LinkedList<Gift> giftsList;
    public static SparseArray<HashMap<String, String>> photoAlbum;
    public static int GRID_COLUMN;
    public static int screen_width;
    public static long midnight;
    // Data cache
    public static LinkedList<Top> topsList;
    public static LinkedList<Dialog> dialogList;
    public static LinkedList<FeedLike> likesList;
    public static LinkedList<FeedInbox> inboxList;
    public static LinkedList<FeedSympathy> mutualList;
    //---------------------------------------------------------------------------
    public static void init(Context context) {
        //removeSSID(context); // for test
        loadSSID(context);

        // Facebook Connection
        facebook = new Facebook(Static.AUTH_FACEBOOK_ID);

        // Data Cache
        if (topsList == null)
            topsList = new LinkedList<Top>();

        if (dialogList == null)
            dialogList = new LinkedList<Dialog>();

        if (inboxList == null)
            inboxList = new LinkedList<FeedInbox>();

        if (likesList == null)
            likesList = new LinkedList<FeedLike>();

        if (mutualList == null)
            mutualList = new LinkedList<FeedSympathy>();
        
        if (giftsList == null)
            giftsList = new LinkedList<Gift>();

        screen_width = (Device.getOrientation(context) == Device.LANDSCAPE) ? Device.getDisplay(context).getHeight() : Device.getDisplay(context).getWidth();

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
        
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        
        midnight = cal.getTimeInMillis();
        
        LocalBroadcastManager.getInstance(context).registerReceiver(new ReAuthReceiver(), new IntentFilter(ReAuthReceiver.REAUTH_INTENT));
    }
    //---------------------------------------------------------------------------
    public static void release() {
        if (topsList != null)
            topsList.clear();
        if (inboxList != null)
            inboxList.clear();
        if (likesList != null)
            likesList.clear();
        if (mutualList != null)
            mutualList.clear();
    }
    //---------------------------------------------------------------------------
    public static boolean isSSID() {
        return SSID != null && SSID.length() > 0;
    }
    //---------------------------------------------------------------------------
    public static String loadSSID(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(Static.PREFERENCES_TAG_SHARED, Context.MODE_PRIVATE);
        SSID = preferences.getString(Static.PREFERENCES_SSID, Static.EMPTY);
        return SSID;
    }
    //---------------------------------------------------------------------------
    public static void saveSSID(Context context,String ssid) {
        SSID = (ssid == null || ssid.length() == 0) ? Static.EMPTY : ssid;
        SharedPreferences preferences = context.getSharedPreferences(Static.PREFERENCES_TAG_SHARED, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(Static.PREFERENCES_SSID, SSID);
        editor.commit();
    }
    //---------------------------------------------------------------------------
    public static void removeSSID(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(Static.PREFERENCES_TAG_SHARED, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(Static.PREFERENCES_SSID, SSID = Static.EMPTY);
        editor.commit();
    }
    //---------------------------------------------------------------------------
}
