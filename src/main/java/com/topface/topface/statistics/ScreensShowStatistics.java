package com.topface.topface.statistics;

import com.topface.framework.utils.Debug;

/**
 * Created by ppetr on 14.03.16.
 * Send statistics when fragment/activity/popup show
 */
public class ScreensShowStatistics {
    private static final String ACTIVITY_SHOWN = "activity_%s_shown";
    private static final String FRAGMENT_SHOWN = "fragment_%s_shown";
    private static final String POPUP_SHOWN = "popup_%s_shown";

    private static void send(String command, String name) {
        if (!name.isEmpty()) {
            Debug.error("STAT " + String.format(command, name.toLowerCase()));
//        StatisticsTracker.getInstance()
//                .sendEvent(String.format(command, name.toLowerCase()), 1);
        }
    }

    public static void sendActivityShow(String activityName) {
        send(ACTIVITY_SHOWN, activityName);
    }

    public static void sendFragmentShow(String fragmentName) {
        send(FRAGMENT_SHOWN, fragmentName);
    }

    public static void sendPopupShow(String popupName) {
        send(POPUP_SHOWN, popupName);
    }
}
