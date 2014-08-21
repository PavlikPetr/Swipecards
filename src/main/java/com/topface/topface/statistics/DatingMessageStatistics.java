package com.topface.topface.statistics;

import com.topface.statistics.android.StatisticsTracker;
import com.topface.topface.App;

/**
 * Dating instant message statistics
 */
public class DatingMessageStatistics {

    public static final String DATING_MESSAGE_SENT = "mobile_dating_message_sent";
    public static final String VIP_BUY_SCREEN_TRANSITION = "mobile_vip_buy_screen_transition";

    private static void send(String key) {
        StatisticsTracker.getInstance().setContext(App.getContext()).sendEvent(key, 1);
    }

    public static void sendDatingMessageSent() {
        send(DATING_MESSAGE_SENT);
    }

    public static void sendVipBuyScreenTransition() {
        send(VIP_BUY_SCREEN_TRANSITION);
    }

}
