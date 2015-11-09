package com.topface.topface.statistics;

import com.topface.statistics.android.Slices;
import com.topface.statistics.android.StatisticsTracker;
import com.topface.topface.utils.CacheProfile;

/**
 * Sending statistics about push button for open settings in popup "Not valid certificate" (SslHandshakeException)
 */
public class ExpressMessagesStastics {

    public static final String OPEN_EXPRESS_MESSAGES = "open_express_messages";
    public static final String VERSION = "val";

    public static void send() {
        StatisticsTracker.getInstance().sendEvent(OPEN_EXPRESS_MESSAGES, 1, getSlices());
    }

    public static Slices getSlices() {
        int vers = CacheProfile.getOptions().premiumMessages.getPopupVersion();
        return new Slices()
                .putSlice(VERSION, "popup_".concat(String.valueOf(vers)));
    }
}
