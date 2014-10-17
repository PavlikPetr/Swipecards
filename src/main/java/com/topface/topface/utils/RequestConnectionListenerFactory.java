package com.topface.topface.utils;

import com.topface.statistics.android.StatisticsTracker;

/**
 * Factory that creates request connection listener based on type of statistics it should send
 */
public class RequestConnectionListenerFactory {

    public IRequestConnectionListener create(String serviceName) {
        if (StatisticsTracker.getInstance().getConfiguration().connectionStatisticsEnabled) {
            return new RequestConnectionListener(serviceName);
        } else {
            return new EmptyRequestConnectionListener();
        }
    }
}
