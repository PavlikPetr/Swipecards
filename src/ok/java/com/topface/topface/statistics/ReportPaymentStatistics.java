package com.topface.topface.statistics;

import com.topface.statistics.android.StatisticsTracker;

/**
 * Created by ppavlik on 15.04.16.
 * Send report payment request statistics
 */
public class ReportPaymentStatistics {
    private static final String SUCCESS_RESPONSE = "success_response";
    private static final String FAIL_RESPONSE = "fail_response";

    private static void send(String state) {
        StatisticsTracker.getInstance()
                .sendEvent(state, 1);
    }

    public static void sendSuccess() {
        send(SUCCESS_RESPONSE);
    }

    public static void sendFail() {
        send(FAIL_RESPONSE);
    }
}
