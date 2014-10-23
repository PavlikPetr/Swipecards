package com.topface.topface.utils;

import com.topface.statistics.android.StatisticsConfiguration;
import com.topface.statistics.android.StatisticsTracker;

/**
 * Factory that creates request connection listener based on type of statistics it should send
 */
public class RequestConnectionListenerFactory {

    public static IRequestConnectionListener create(String serviceName) {
        StatisticsConfiguration configuration = StatisticsTracker.getInstance().getConfiguration();
        if (configuration != null && configuration.connectionStatisticsEnabled) {
            return new RequestConnectionListener(serviceName);
        } else {
            return new EmptyRequestConnectionListener();
        }
    }
}
