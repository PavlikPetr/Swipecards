package com.topface.statistics.android;

/**
 * Created by kirussell on 23.04.2014.
 */
public class StatisticsConfiguration {
    public boolean statisticsEnabled = false;
    public int maxHitsDispatch = 200;
    public long maxDispatchExpireDelay = 180000;
    public String userAgent = "";

    public StatisticsConfiguration(boolean enabled, int maxDispatch, long maxDelay, String userAgent) {
        statisticsEnabled = enabled;
        maxHitsDispatch = maxDispatch;
        maxDispatchExpireDelay = maxDelay;
        this.userAgent = userAgent;
    }
}
