package com.topface.topface.statistics;

import com.topface.statistics.android.Slices;
import com.topface.statistics.android.StatisticsTracker;

/**
 * Sending statistics about push button "Buy VIP".
 */
public class PushButtonVipStatistics {

    public static final String PUSH_BUTTON_VIP = "buy_button_click";

    public static final String BUTTON_TYPE = "val";
    public static final String CLASS_NAME = "ref";
    public static final String FROM_SCREEN_NAME = "plc";

    public static void send(String button_type, String class_name, String from_screen_name) {
        StatisticsTracker.getInstance().sendEvent(PUSH_BUTTON_VIP, 1, generateSlices(button_type, class_name, from_screen_name));
    }

    public static Slices generateSlices(String button_type, String class_name, String from_screen_name) {
        return new Slices()
                .putSlice(BUTTON_TYPE, button_type)
                .putSlice(CLASS_NAME, class_name)
                .putSlice(FROM_SCREEN_NAME, from_screen_name);
    }
}
