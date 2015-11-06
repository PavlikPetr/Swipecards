package com.topface.topface.statistics;

import com.topface.statistics.android.StatisticsTracker;

/**
 * Sending statistics about push button for open settings in popup "Not valid certificate" (SslHandshakeException)
 */
public class ExpressMessagesStastics {

    public static final String OPEN_EXPRESS_MESSAGES = "open_express_messages";

    public static void send() {
        StatisticsTracker.getInstance().sendEvent(OPEN_EXPRESS_MESSAGES, 1);
    }
}
