package com.topface.topface.statistics;

import com.topface.statistics.android.StatisticsTracker;

/**
 * Created by ppetr on 14.03.16.
 * Send statistics with foreground/background action
 */
public class AppStateStatistics {
    private static final String APP_GO_FOREGROUND = "app_in_foreground";
    private static final String APP_GO_BACKGROUND = "app_in_background";

    private static void send(String command) {
        StatisticsTracker.getInstance()
                .sendEvent(command, 1);
    }

    public static void sendAppForegroundState() {
        send(APP_GO_FOREGROUND);
    }

    public static void sendAppBackgroundState() {
        send(APP_GO_BACKGROUND);
    }
}
