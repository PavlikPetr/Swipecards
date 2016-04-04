package com.topface.topface.data;

/**
 * Created by ppavlik on 04.04.16.
 * hold state and name of current activity
 */
public class ActivityLifreCycleData {
    public enum ActivityLifecycle {
        RESUMED, PAUSED, STOPPED, SAVE_INSTANCE_STATE, DESTROYED, CREATED, STARTED
    }

    public String activityName;
    public ActivityLifecycle state;

    public ActivityLifreCycleData(String activityName, ActivityLifecycle state) {
        this.activityName = activityName;
        this.state = state;
    }
}
