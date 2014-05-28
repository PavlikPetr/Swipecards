package com.topface.statistics.android;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.topface.statistics.Hit;
import com.topface.statistics.IDataDispatcher;
import com.topface.statistics.ILogger;
import com.topface.statistics.INetworkClient;
import com.topface.statistics.NetworkDataDispatcher;
import com.topface.statistics.Statistics;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by kirussell on 22.04.2014.
 * Android statistics tracker wraps Statistics instance.
 * NOTE: needs Context for work
 */
@SuppressWarnings("UnusedDeclaration")
public class StatisticsTracker {

    public static final String TAG = "TFAndroidTracker";
    private static final Map<String, String> mPredefinedSlices = new HashMap<>();
    private static volatile StatisticsTracker mInstance;
    private Statistics mStatistics;
    private Context mContext;
    private INetworkClient mNetworkClient;
    private int mActiveActivities;
    private boolean mEnabled;
    private ILogger mLogger;
    private IDataDispatcher mNetworkDispatcher;
    private StatisticsConfiguration mConfiguration;

    private StatisticsTracker() {
        mNetworkClient = new NetworkHttpClient();
        mNetworkDispatcher = new NetworkDataDispatcher(mNetworkClient);
        mStatistics = new Statistics(mNetworkDispatcher);
    }

    public static StatisticsTracker getInstance() {
        StatisticsTracker localInstance = mInstance;
        if (localInstance == null) {
            synchronized (StatisticsTracker.class) {
                localInstance = mInstance;
                if (localInstance == null) {
                    mInstance = localInstance = new StatisticsTracker();
                }
            }
        }
        return localInstance;
    }

    public StatisticsTracker setConfiguration(StatisticsConfiguration configuration) {
        if (configuration != null) {
            mConfiguration = configuration;
            mEnabled = mConfiguration.statisticsEnabled;
            mStatistics
                    .setMaxHitsDispatch(mConfiguration.maxHitsDispatch)
                    .setMaxDispatchExpireDelay(mConfiguration.maxDispatchExpireDelay);
            mNetworkClient.setUserAgent(mConfiguration.userAgent);
            if (mConfiguration.statisticsUrl != null) {
                mNetworkClient.setUrl(mConfiguration.statisticsUrl);
            }
        }
        return this;
    }

    public StatisticsConfiguration getConfiguration() {
        return mConfiguration;
    }

    public StatisticsTracker setContext(Context context) {
        if (context == null) {
            Log.d(TAG, "Passed context can not be null!");
        } else if (mContext == null) {
            mContext = context.getApplicationContext();
            initContextRelatedEntities(mContext);
        }
        return this;
    }

    private void initContextRelatedEntities(Context context) {
        mStatistics.setStorage(new SharedPreferencesStorage(context));
    }

    public void activityStart(Activity activity) {
        setContext(activity);
        mActiveActivities++;
        if (mActiveActivities == 1 && mEnabled) {
            mStatistics.onStart();
        }
    }

    public void activityStop(Activity activity) {
        setContext(activity);
        mActiveActivities--;
        if (mActiveActivities == 0 && mEnabled) {
            mStatistics.onStop();
        }
    }

    /**
     * Slices common for all sent events
     *
     * @param key   slice name
     * @param value slice value
     * @return self
     */
    public StatisticsTracker putPredefinedSlice(String key, String value) {
        mPredefinedSlices.put(key, value);
        return this;
    }

    /**
     * Sends event to statistics queue
     *
     * @param name   event name
     * @param count  event count value
     * @param slices event slices
     */
    public void sendEvent(String name, Integer count, Slices slices) {
        if (mEnabled && mContext != null) {
            mStatistics.sendHit(new AndroidHit(name, count, slices));
        }
    }

    /**
     * Sends event to statistics queue without slices (predefined slices still be sent)
     *
     * @param name  event name
     * @param count event count value
     */
    public void sendEvent(String name, int count) {
        sendEvent(name, count, null);
    }

    /**
     * Sends event to statistics queue without count (count field will be excepted from sent data)
     *
     * @param name   event name
     * @param slices event slices
     */
    public void sendEvent(String name, Slices slices) {
        sendEvent(name, null, slices);
    }

    public StatisticsTracker setLogger(ILogger logger) {
        this.mLogger = logger;
        mStatistics.setLogger(mLogger);
        mNetworkDispatcher.setLogger(mLogger);
        return this;
    }

    private class AndroidHit extends Hit {

        public AndroidHit(String name, Integer count, Map<String, String> slices) {
            super(name, count, slices);
            addAllSlice(mPredefinedSlices);
        }
    }
}
