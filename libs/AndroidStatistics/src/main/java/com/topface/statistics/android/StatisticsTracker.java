package com.topface.statistics.android;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import com.topface.statistics.Hit;
import com.topface.statistics.INetworkClient;
import com.topface.statistics.NetworkDataDispatcher;
import com.topface.statistics.Statistics;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by kirussell on 22.04.2014.
 */
public class StatisticsTracker {

    static final String TAG = "TFAndroidTracker";
    private static final Map<String, String> mPredefinedSlices = new HashMap<>();
    private static volatile StatisticsTracker mInstance;
    private Statistics mStatistics;
    private Context mContext;
    private INetworkClient mNetworkClient;
    private int mActiveActivities;
    private boolean mEnabled;

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

    public StatisticsTracker addPredefinedSlices(String key, String value) {
        mPredefinedSlices.put(key, value);
        return this;
    }

    public void addPredefinedSlices(Slices slices) {
        mPredefinedSlices.putAll(slices);
    }

    public void sendEvent(String name, int count, Slices slices) {
        if (mEnabled && mContext != null) {
            mStatistics.sendHit(new AndroidHit(name, count, slices));
        }
    }

    public void sendEvent(String name, int count) {
        sendEvent(name, count, null);
    }

    private class AndroidHit extends Hit {

        public AndroidHit(String name, int count, Map<String, String> slices) {
            super(name, count, slices);
            addAllSlice(mPredefinedSlices);
        }
    }
}
