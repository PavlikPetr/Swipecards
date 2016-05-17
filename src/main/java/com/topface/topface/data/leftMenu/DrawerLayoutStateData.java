package com.topface.topface.data.leftMenu;

import android.support.annotation.IntDef;

/**
 * Created by ppavlik on 17.05.16.
 */
public class DrawerLayoutStateData {

    public static final int UNDEFINED = 0;
    public static final int STATE_CHANGED = 1;
    public static final int SLIDE = 2;
    public static final int OPENED = 3;
    public static final int CLOSED = 4;

    @IntDef({UNDEFINED,STATE_CHANGED,SLIDE,OPENED,CLOSED})
    public @interface DrawerLayoutState1 {
    }

    @DrawerLayoutState1
    private int mState;

    public DrawerLayoutStateData(@DrawerLayoutState1 int state){
        mState = state;
    }

    public int getState(){
        return  mState;
    }
}
