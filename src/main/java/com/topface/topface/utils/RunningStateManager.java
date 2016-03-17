package com.topface.topface.utils;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by ppetr on 14.03.16.
 * We need it to detect moment when app go background/foreground
 */
public class RunningStateManager {
    private ConcurrentHashMap<String, Long> mActivitiesState;
    private List<OnAppChangeStateListener> mOnAppChangeStateListeners;
    private long mAppStartTime;

    public void onActivityStarted(String activityName) {
        long timeStart = getCurrentTime();
        synchronized (getActivitiesState()) {
            if (getActivitiesState().isEmpty()) {
                mAppStartTime = timeStart;
                callOnAppForeground();
            }
            getActivitiesState().put(activityName, timeStart);
        }
    }

    public void onActivityStoped(String activityName) {
        synchronized (getActivitiesState()) {
            getActivitiesState().remove(activityName);
            if (getActivitiesState().isEmpty()) {
                callOnAppBackground();
            }
        }
    }

    @NotNull
    private ConcurrentHashMap<String, Long> getActivitiesState() {
        if (mActivitiesState == null) {
            mActivitiesState = new ConcurrentHashMap<>();
        }
        return mActivitiesState;
    }

    @NotNull
    private List<OnAppChangeStateListener> getInterfacesList() {
        if (mOnAppChangeStateListeners == null) {
            mOnAppChangeStateListeners = Collections.synchronizedList(new ArrayList<OnAppChangeStateListener>());
        }
        return mOnAppChangeStateListeners;
    }


    private long getCurrentTime() {
        return Calendar.getInstance().getTimeInMillis();
    }

    private void callOnAppForeground() {
        synchronized (getInterfacesList()) {
            for (OnAppChangeStateListener listener : getInterfacesList()) {
                if (listener != null) {
                    listener.onAppForeground(mAppStartTime);
                }
            }
        }
    }

    private void callOnAppBackground() {
        synchronized (getInterfacesList()) {
            long timeStop = getCurrentTime();
            for (OnAppChangeStateListener listener : getInterfacesList()) {
                if (listener != null) {
                    listener.onAppBackground(timeStop, mAppStartTime);
                }
            }
        }
    }

    public void registerAppChangeStateListener(@NotNull OnAppChangeStateListener listener) {
        if (!getInterfacesList().contains(listener)) {
            synchronized (getInterfacesList()) {
                getInterfacesList().add(listener);
            }
            if (mAppStartTime != 0) {
                listener.onAppForeground(mAppStartTime);
            }
        }
    }

    public void unregisterAppChangeStateListener(OnAppChangeStateListener listener) {
        synchronized (getInterfacesList()) {
            getInterfacesList().remove(listener);
        }
    }

    public interface OnAppChangeStateListener {
        void onAppForeground(long timeOnStart);

        void onAppBackground(long timeOnStop, long timeOnStart);
    }
}
