package com.topface.topface.utils;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

/**
 * Created by ppetr on 14.03.16.
 * We need it to detect moment when app go background/foreground
 */
public class RunningStateManager {
    private HashMap<String, Long> mActivitiesState;
    private ArrayList<OnAppChangeStateListener> mOnAppChangeStateListeners;
    private long mAppStartTime;
    private static RunningStateManager mInstance;

    public static RunningStateManager getInstance() {
        if (mInstance == null) {
            mInstance = new RunningStateManager();
        }
        return mInstance;
    }

    private RunningStateManager() {
        mActivitiesState = new HashMap<>();
        mOnAppChangeStateListeners = new ArrayList<>();
    }

    public void onActivityStarted(String activityName) {
        long timeStart = getCurrentTime();
        if (mActivitiesState.isEmpty()) {
            mAppStartTime = timeStart;
            callOnAppForeground();
        }
        mActivitiesState.put(activityName, timeStart);
    }

    public void onActivityStoped(String activityName) {
        mActivitiesState.remove(activityName);
        if (mActivitiesState.isEmpty()) {
            callOnAppBackground();
        }
    }

    private long getCurrentTime() {
        return Calendar.getInstance().getTimeInMillis();
    }

    public static boolean isAppForeground() {
        return getInstance().mActivitiesState != null && !getInstance().mActivitiesState.isEmpty();
    }

    private void callOnAppForeground() {
        if (mOnAppChangeStateListeners != null) {
            for (OnAppChangeStateListener listener : mOnAppChangeStateListeners) {
                listener.onAppForeground(mAppStartTime);
            }
        }
    }

    private void callOnAppBackground() {
        if (mOnAppChangeStateListeners != null) {
            long timeStop = getCurrentTime();
            for (OnAppChangeStateListener listener : mOnAppChangeStateListeners) {
                listener.onAppBackground(timeStop, mAppStartTime);
            }
        }
    }

    public void registerAppChangeStateListener(@NotNull OnAppChangeStateListener listener) {
        if (!mOnAppChangeStateListeners.contains(listener)) {
            mOnAppChangeStateListeners.add(listener);
            if (mAppStartTime != 0) {
                listener.onAppForeground(mAppStartTime);
            }
        }
    }

    public void unregisterAppChangeStateListener(OnAppChangeStateListener listener) {
        mOnAppChangeStateListeners.remove(listener);
    }

    public interface OnAppChangeStateListener {
        void onAppForeground(long timeOnStart);

        void onAppBackground(long timeOnStop, long timeOnStart);
    }
}
