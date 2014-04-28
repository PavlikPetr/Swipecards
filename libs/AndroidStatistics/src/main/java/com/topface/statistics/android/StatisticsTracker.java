package com.topface.statistics.android;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import com.topface.statistics.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by kirussell on 22.04.2014.
 * Android statistics tracker wraps Statistics instance.
 * NOTE: needs Context for work
 */
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

    private StatisticsTracker() {
        mNetworkClient = new NetworkHttpClient();
        mStatistics = new Statistics(
                new NetworkDataDispatcher(
                        mNetworkClient
                )
        );
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
        mEnabled = configuration.statisticsEnabled;
        mStatistics
                .setMaxHitsDispatch(configuration.maxHitsDispatch)
                .setMaxDispatchExpireDelay(configuration.maxDispatchExpireDelay);
        mNetworkClient.setUserAgent(configuration.userAgent);
        return this;
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
        return this;
    }

    private class AndroidHit extends Hit {

        public AndroidHit(String name, Integer count, Map<String, String> slices) {
            super(name, count, slices);
            addAllSlice(mPredefinedSlices);
        }
    }
}
