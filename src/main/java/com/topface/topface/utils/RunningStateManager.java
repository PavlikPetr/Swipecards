package com.topface.topface.utils;

import com.topface.topface.App;
import com.topface.topface.data.ActivityLifreCycleData;
import com.topface.topface.state.LifeCycleState;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;

import rx.functions.Action1;
import rx.functions.Func1;

/**
 * Created by ppetr on 14.03.16.
 * We need it to detect moment when app go background/foreground
 */
public class RunningStateManager {
    @Inject
    LifeCycleState mLifeCycleState;
    private ConcurrentHashMap<String, Long> mActivitiesState;
    private List<OnAppChangeStateListener> mOnAppChangeStateListeners;
    private long mAppStartTime;

    public RunningStateManager() {
        App.get().inject(this);
        mLifeCycleState.getObservable(ActivityLifreCycleData.class)
                .filter(new Func1<ActivityLifreCycleData, Boolean>() {
                    @Override
                    public Boolean call(ActivityLifreCycleData activityLifreCycleData) {
                        return activityLifreCycleData != null
                                && (activityLifreCycleData.getState() == ActivityLifreCycleData.STARTED
                                || activityLifreCycleData.getState() == ActivityLifreCycleData.STOPPED);
                    }
                })
                .subscribe(new Action1<ActivityLifreCycleData>() {
                    @Override
                    public void call(ActivityLifreCycleData activityLifreCycleData) {
                        if (activityLifreCycleData.getState() == ActivityLifreCycleData.STARTED) {
                            onActivityStarted(activityLifreCycleData.getClassName());
                        } else if (activityLifreCycleData.getState() == ActivityLifreCycleData.STOPPED) {
                            onActivityStoped(activityLifreCycleData.getClassName());
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {

                    }
                });
    }

    private void onActivityStarted(String activityName) {
        long timeStart = getCurrentTime();
        synchronized (getActivitiesState()) {
            if (getActivitiesState().isEmpty()) {
                mAppStartTime = timeStart;
                callOnAppForeground();
            }
            getActivitiesState().put(activityName, timeStart);
        }
    }

    private void onActivityStoped(String activityName) {
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
