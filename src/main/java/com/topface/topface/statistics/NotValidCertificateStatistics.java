package com.topface.topface.statistics;

import com.topface.statistics.android.StatisticsTracker;

/**
 * Sending statistics about push button for open settings in popup "Not valid certificate" (SslHandshakeException)
 */
public class NotValidCertificateStatistics {

    public static final String PUSH_BUTTON_MOBILE_SSL_ERROR_CLICK = "mobile_ssl_error_click";

    public static void send() {
        StatisticsTracker.getInstance().sendEvent(PUSH_BUTTON_MOBILE_SSL_ERROR_CLICK, 1);
    }
}
