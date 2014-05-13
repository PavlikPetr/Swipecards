package com.topface.statistics.android;

/**
 * Created by kirussell on 23.04.2014.
 * Some configuration data to pass to tracker
 */
public class StatisticsConfiguration {
    public boolean statisticsEnabled = false;
    public int maxHitsDispatch = 200;
    public long maxDispatchExpireDelay = 180000;
    public String userAgent = "";
    public String statisticsUrl;

    public StatisticsConfiguration(boolean enabled, int maxDispatch, long maxDelay, String userAgent) {
        statisticsEnabled = enabled;
        maxHitsDispatch = maxDispatch;
        maxDispatchExpireDelay = maxDelay;
        this.userAgent = userAgent;
        statisticsUrl = null;
    }

    public StatisticsConfiguration(boolean enabled, int maxDispatch, long maxDelay, String userAgent, String url) {
        statisticsEnabled = enabled;
        maxHitsDispatch = maxDispatch;
        maxDispatchExpireDelay = maxDelay;
        this.userAgent = userAgent;
        this.statisticsUrl = url;
    }
}
