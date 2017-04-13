package com.topface.topface.data.leftMenu;

import android.support.annotation.IntDef;

public class DrawerLayoutStateData {

    public static final int UNDEFINED = 0;
    public static final int SLIDE = 1;
    public static final int OPENED = 2;
    public static final int CLOSED = 3;

    @IntDef({UNDEFINED, SLIDE, OPENED, CLOSED})
    public @interface DrawerLayoutState {
    }

    @DrawerLayoutState
    private int mState;

    public DrawerLayoutStateData(@DrawerLayoutState int state) {
        mState = state;
    }

    public int getState() {
        return mState;
    }

    @Override
    public int hashCode() {
        return mState;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof DrawerLayoutStateData && mState == ((DrawerLayoutStateData) o).getState();
    }
}
