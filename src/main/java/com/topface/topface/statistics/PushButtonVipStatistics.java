package com.topface.topface.statistics;

import android.util.Log;

import com.topface.statistics.android.Slices;
import com.topface.statistics.android.StatisticsTracker;
import com.topface.topface.App;

/**
 * Sending statistics about push button "Buy VIP".
 */
public class PushButtonVipStatistics {

    public static final String PUSH_BUTTON_VIP = "buy_button_click";

    public static final String BUTTON_TYPE = "val";
    public static final String CLASS_NAME = "ref";
    public static final String FROM_SCREEN_NAME = "plc";

    public static void send(String button_type, String class_name, String from_screen_name) {
        StatisticsTracker.getInstance().setContext(App.getContext()).sendEvent(PUSH_BUTTON_VIP, 1, generateSlices(button_type, class_name, from_screen_name));
    }

    public static Slices generateSlices(String button_type, String class_name, String from_screen_name) {
        Log.e("TOP_FACE", "generateSlices button_type: " + button_type + " class_name: " + class_name + " from_screen_name: " + from_screen_name);
        Slices slices = new Slices();
        slices.putSlice(BUTTON_TYPE, button_type);
        slices.putSlice(CLASS_NAME, class_name);
        slices.putSlice(FROM_SCREEN_NAME, from_screen_name);
        return slices;
    }
}
