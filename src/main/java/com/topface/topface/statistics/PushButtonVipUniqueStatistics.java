package com.topface.topface.statistics;

import com.topface.statistics.android.Slices;
import com.topface.statistics.android.StatisticsTracker;
import com.topface.topface.App;
import com.topface.topface.data.Profile;

/**
 * Sending unique statistics about push button "Buy VIP".
 */
public class PushButtonVipUniqueStatistics {

    public static final String PUSH_BUTTON_VIP_UNIQUE = "buy_button_click_vip_unique";
    public static final String PUSH_BUTTON_NO_VIP_UNIQUE = "buy_button_click_novip_unique";

    private static void sendStatistic(String key, Slices slices, String unique) {
        StatisticsTracker.getInstance().setContext(App.getContext()).sendUniqueEvent(key, 1, slices, unique + "_" + key);
    }

    public static void sendPushButtonVip(String button_type, String class_name, String from_screen_name, Profile profile) {
        sendStatistic(PUSH_BUTTON_VIP_UNIQUE, PushButtonVipStatistics.generateSlices(button_type, class_name, from_screen_name), Integer.toString(profile.uid));
    }

    public static void sendPushButtonNoVip(String button_type, String class_name, String from_screen_name, Profile profile) {
        sendStatistic(PUSH_BUTTON_NO_VIP_UNIQUE, PushButtonVipStatistics.generateSlices(button_type, class_name, from_screen_name), Integer.toString(profile.uid));
    }
}
