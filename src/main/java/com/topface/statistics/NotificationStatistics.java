package com.topface.statistics;

import android.os.Build;

import com.topface.statistics.android.Slices;
import com.topface.statistics.android.StatisticsTracker;
import com.topface.topface.App;

/**
 * Sending statistics about push notifications.
 */
public class NotificationStatistics {

    public static final String RECEIVED_KEY = "mobile_push_received";
    public static final String OPENED_KEY = "mobile_push_open";
    public static final String DELETED_KEY = "mobile_push_delete";

    public static final String NOTIFICATION_TYPE = "val";
    public static final String NOTIFICATION_LABEL = "ref";
    public static final String API_LEVEL = "plc";

    private static void send(String key, int type, String label) {
        send(key, String.valueOf(type), label);
    }

    private static void send(String key, String type, String label) {
        /*
        If there is no label, no statistics sent.
         */
        if (label == null) {
            return;
        }
        StatisticsTracker.getInstance().setContext(App.getContext()).
                sendEvent(key, 1, new Slices().
                        putSlice(NOTIFICATION_TYPE, type).
                        putSlice(NOTIFICATION_LABEL, label).
                        putSlice(API_LEVEL, String.valueOf(Build.VERSION.SDK_INT)));
    }

    public static void sendReceived(String type, String label) {
        send(RECEIVED_KEY, type, label);
    }

    public static void sendReceived(int type, String label) {
        send(RECEIVED_KEY, type, label);
    }

    public static void sendOpened(String type, String label) {
        send(OPENED_KEY, type, label);
    }

    public static void sendOpened(int type, String label) {
        send(OPENED_KEY, type, label);
    }

    public static void sendDeleted(String type, String label) {
        send(DELETED_KEY, type, label);
    }

    public static void sendDeleted(int type, String label) {
        send(DELETED_KEY, type, label);
    }
}
