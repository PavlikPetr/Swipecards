package com.topface.topface.statistics;

import com.topface.framework.utils.Debug;
import com.topface.statistics.android.Slices;
import com.topface.statistics.android.StatisticsTracker;

/**
 * Created by ppetr on 14.03.16.
 * Send statistics when fragment/activity/popup show
 */
public class ScreensShowStatistics {
    private static final String SCREEN_SHOW = "screen_show";
    private static final String POPUP_SHOW = "popup_show";
    private static final String SLICE_PLC = "plc";

    private static void send(String command, String screenName) {
        if (!screenName.isEmpty()) {
            // логи для тестеров, после теста задачи #46897 можно удалить
            Debug.log("ScreensShowStatistics", command + " " + SLICE_PLC + ":" + screenName);
            Slices slices = new Slices();
            slices.put(SLICE_PLC, screenName.toLowerCase());
            StatisticsTracker.getInstance()
                    .sendEvent(command, 1, slices);
        }
    }

    public static void sendScreenShow(String screenName) {
        send(SCREEN_SHOW, screenName);
    }

    public static void sendPopupShow(String popupName) {
        send(POPUP_SHOW, popupName);
    }
}
