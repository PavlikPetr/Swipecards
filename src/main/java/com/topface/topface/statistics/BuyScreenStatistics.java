package com.topface.topface.statistics;

import com.topface.statistics.android.Slices;
import com.topface.statistics.android.StatisticsTracker;
import com.topface.topface.App;

/**
 * Promo popup show unique statistics
 */
public class BuyScreenStatistics {

    public static final String BUY_SCREEN_SHOW = "buy_screen_show";
    public static final String BUY_SCREEN_SHOW_UNIQUE = "buy_screen_show_unique";
    public static final String TAG = "tag";
    public static final String PLC = "plc";

    private static void sendUniqueStatistics(String screenName, Slices slices) {
        StatisticsTracker
                .getInstance()
                .setContext(App.getContext())
                .sendUniqueEvent(BUY_SCREEN_SHOW_UNIQUE, 1, slices, Integer.toString(App.get().getProfile().uid) + "_" + screenName);
    }

    private static void sendRegularStatistics(Slices slices) {
        StatisticsTracker.getInstance()
                .sendEvent(BUY_SCREEN_SHOW, 1, slices);
    }

    public static void buyScreenShowSendStatistics(String screenName, String screenVersion) {
        if (screenName != null) {
            screenName = screenName.toLowerCase();
        } else {
            return;
        }
        Slices slices = generateSlices(screenVersion, screenName);
        sendUniqueStatistics(screenName, slices);
        sendRegularStatistics(slices);
    }

    private static Slices generateSlices(String version, String screenName) {
        return new Slices()
                .putSlice(TAG, version)
                .putSlice(PLC, screenName);
    }
}