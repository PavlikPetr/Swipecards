package com.topface.topface.utils;

import com.topface.statistics.android.StatisticsConfiguration;
import com.topface.statistics.android.StatisticsTracker;

/**
 * Factory that creates request connection listener based on type of statistics it should send
 */
public class RequestConnectionListenerFactory {

    public static IRequestConnectionListener create(String serviceName) {
        StatisticsConfiguration statisticsConfiguration = StatisticsTracker.getInstance().getConfiguration();
        if (statisticsConfiguration != null && statisticsConfiguration.connectionStatisticsEnabled) {
            return new RequestConnectionListener(serviceName);
        } else {
            return new EmptyRequestConnectionListener();
        }
    }
}
