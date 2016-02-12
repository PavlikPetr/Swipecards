
package com.topface.topface.statistics;

import com.topface.statistics.android.Slices;
import com.topface.statistics.android.StatisticsTracker;

public class RatePopupStatistics {
    private static final String RATE_POPUP_SHOW = "mobile_rate_popup_show";
    private static final String RATE_POPUP_CLOSE = "mobile_rate_popup_close";
    private static final String RATE_POPUP_CLICK_BUTTON_CLOSE = "mobile_rate_popup_click_button_close";
    private static final String RATE_POPUP_CLICK_BUTTON_LATER = "mobile_rate_popup_click_button_later";
    private static final String RATE_POPUP_CLICK_BUTTON_RATE = "mobile_rate_popup_click_button_rate";
    private static final String RATING = "val";

    private static void send(String action) {
        StatisticsTracker.getInstance()
                .sendEvent(action, 1);
    }

    public static void sendRatePopupShow() {
        send(RATE_POPUP_SHOW);
    }

    public static void sendRatePopupClose() {
        send(RATE_POPUP_CLOSE);
    }

    public static void sendRatePopupClickButtonClose() {
        send(RATE_POPUP_CLICK_BUTTON_CLOSE);
    }

    public static void sendRatePopupClickButtonLater() {
        send(RATE_POPUP_CLICK_BUTTON_LATER);
    }

    public static void sendRatePopupClickButtonRate(long rateValue) {
        StatisticsTracker.getInstance().sendEvent(RATE_POPUP_CLICK_BUTTON_RATE, 1, generateSlices(rateValue));
    }

    private static Slices generateSlices(long value) {
        return new Slices()
                .putSlice(RATING, String.valueOf(value));
    }
}

