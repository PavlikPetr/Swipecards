package com.topface.topface.statistics;

import com.topface.statistics.android.Slices;
import com.topface.statistics.android.StatisticsTracker;
import com.topface.topface.App;
import com.topface.topface.utils.CacheProfile;

/**
 * Sending unique statistics about push button "Buy VIP".
 */
public class PushButtonVipUniqueStatistics extends PushButtonVipStatistics {

    public static final String PUSH_BUTTON_VIP_UNIQUE = "mobile_push_button_vip_unique";


    private static void sendStatistic(String key, Slices slices, String unique) {
        StatisticsTracker.getInstance().setContext(App.getContext()).sendUniqueEvent(key, 1, slices, unique + "_" + key);
    }

    @SuppressWarnings("UnusedDeclaration")
    public static void sendPushButtonVip(String unique) {
        sendStatistic(PUSH_BUTTON_VIP_UNIQUE, null, unique);
    }

    @SuppressWarnings("UnusedDeclaration")
    public static void sendPushButtonVip() {
        sendStatistic(PUSH_BUTTON_VIP_UNIQUE, null, Integer.toString(CacheProfile.getProfile().uid));
    }

    public static void sendPushButtonVip(String button_type, String class_name, String from_screen_name) {
        sendStatistic(PUSH_BUTTON_VIP_UNIQUE, generateSlices(button_type, class_name, from_screen_name), Integer.toString(CacheProfile.getProfile().uid));
    }

    @SuppressWarnings("UnusedDeclaration")
    public static void sendPushButtonVip(String unique, String button_type, String class_name, String from_screen_name) {
        sendStatistic(PUSH_BUTTON_VIP_UNIQUE, generateSlices(button_type, class_name, from_screen_name), unique);
    }


}
