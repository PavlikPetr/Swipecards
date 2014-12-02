package com.topface.topface.statistics;

import com.topface.statistics.android.StatisticsTracker;
import com.topface.topface.App;
import com.topface.topface.utils.CacheProfile;

/**
 * Sending unique statistics about push button "Buy VIP".
 */
public class PushButtonVipUniqueStatistics {

    public static final String PUSH_BUTTON_VIP_UNIQUE = "mobile_push_button_vip_unique";


    private static void send(String key, String unique) {
        StatisticsTracker.getInstance().setContext(App.getContext()).sendUniqueEvent(key, 1, unique + "_" + key);
    }

    @SuppressWarnings("UnusedDeclaration")
    public static void sendPushButtonVip(String unique) {
        send(PUSH_BUTTON_VIP_UNIQUE, unique);
    }

    public static void sendPushButtonVip() {
        send(PUSH_BUTTON_VIP_UNIQUE, Integer.toString(CacheProfile.getProfile().uid));
    }

}
