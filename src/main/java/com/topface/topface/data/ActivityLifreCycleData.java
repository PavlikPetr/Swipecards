package com.topface.topface.data;

import android.support.annotation.IntDef;

/**
 * Created by ppavlik on 04.04.16.
 * hold state and name of current activity
 */
public class ActivityLifreCycleData extends ViewLifreCycleData1 {

    public static final int PAUSED = 1;
    public static final int STOPPED = 2;
    public static final int SAVE_INSTANCE_STATE = 3;
    public static final int DESTROYED = 4;
    public static final int CREATED = 5;
    public static final int STARTED = 6;
    public static final int RESTARTED = 7;
    public static final int RESUMED = 8;


    @IntDef({RESUMED, PAUSED, STOPPED, SAVE_INSTANCE_STATE, DESTROYED, CREATED, STARTED, RESTARTED})
    @interface ActivityLifecycle {

    }

    @ActivityLifecycle
    private int mState;

    public ActivityLifreCycleData(String className, @ActivityLifecycle int state) {
        super(className);
        mState = state;
    }

    public void setState(@ActivityLifecycle int state) {
        mState = state;
    }

    @ActivityLifecycle
    public int getState() {
        return mState;
    }
}
