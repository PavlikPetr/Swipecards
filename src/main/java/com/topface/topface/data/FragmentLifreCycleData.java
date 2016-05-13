package com.topface.topface.data;

import android.support.annotation.IntDef;

/**
 * Created by ppavlik on 04.04.16.
 * hold state and name of current activity
 */
public class FragmentLifreCycleData extends ViewLifreCycleData1 {

    public static final int DESTROY_VIEW = 1;
    public static final int ATTACH = 2;
    public static final int CREATE = 3;
    public static final int CREATE_VIEW = 4;
    public static final int VIEW_CREATED = 5;
    public static final int START = 6;
    public static final int RESUME = 7;
    public static final int SAVE_INSTANCE_STATE = 8;
    public static final int PAUSE = 9;
    public static final int STOP = 10;
    public static final int DESTROY = 11;
    public static final int DETACH = 12;


    @IntDef({DESTROY_VIEW, ATTACH, CREATE, CREATE_VIEW, VIEW_CREATED, START, RESUME,
            SAVE_INSTANCE_STATE, PAUSE, STOP, DESTROY, DETACH})
    @interface FragmentLifecycle {

    }

    @FragmentLifecycle
    private int mState;

    public FragmentLifreCycleData(String className, @FragmentLifecycle int state) {
        super(className);
        mState = state;
    }

    public void setState(@FragmentLifecycle int state) {
        mState = state;
    }

    @FragmentLifecycle
    public int getState() {
        return mState;
    }
}
