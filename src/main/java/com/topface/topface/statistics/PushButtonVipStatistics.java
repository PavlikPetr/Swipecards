package com.topface.topface.statistics;

import com.topface.statistics.android.Slices;
import com.topface.statistics.android.StatisticsTracker;
import com.topface.topface.App;

/**
 * Sending statistics about push button "Buy VIP".
 */
public class PushButtonVipStatistics {

    public static final String PUSH_BUTTON_VIP = "mobile_push_button_vip";

    public static final String BUTTON_TYPE = "val";
    public static final String TAB_NAME = "ref";
    public static final String FROM_SCREEN_NAME = "plc";

    public static void send(String button_type, String tab_name, String from_screen_name) {
        Slices slices = new Slices();
        slices.putSlice(BUTTON_TYPE, button_type);
        slices.putSlice(TAB_NAME, tab_name);
        slices.putSlice(FROM_SCREEN_NAME, from_screen_name);
        StatisticsTracker.getInstance().setContext(App.getContext()).sendEvent(PUSH_BUTTON_VIP, 1, slices);
    }
}
