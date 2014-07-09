package com.topface.statistics;

import com.topface.statistics.android.Slices;
import com.topface.statistics.android.StatisticsTracker;
import com.topface.topface.App;

/**
 * Sending statistics about push notifications.
 */
public class NotificationStatistics {

    public static final String SEND_KEY = "mobile_push_send";
    public static final String OPEN_KEY = "mobile_push_open";
    public static final String DELETE_KEY = "mobile_push_delete";

    public static final String NOTIFICATION_TYPE = "val";
    public static final String NOTIFICATION_LABEL = "plc";

    public static void send(String key, int type, String label) {
        send(key, String.valueOf(type), label);
    }

    public static void send(String key, String type, String label) {
        StatisticsTracker.getInstance().setContext(App.getContext()).
                sendEvent(key, 1, new Slices().
                        putSlice(NOTIFICATION_TYPE, type).
                        putSlice(NOTIFICATION_LABEL, label));
    }
}
