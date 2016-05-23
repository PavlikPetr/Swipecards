package com.topface.topface.data.leftMenu;

import android.support.annotation.IntDef;

import java.util.ArrayList;

/**
 * Created by ppavlik on 16.05.16.
 * Wrap LeftMenuSettingsData with added sender type to object
 */
public class WrappedNavigationData {

    public final static int SELECT_EXTERNALY = 1;
    public final static int SELECT_BY_CLICK = 2;
    public final static int SELECT_ONLY = 3;
    public final static int ITEM_SELECTED = 4;
    public final static int SWITCH_EXTERNALLY = 5;
    public final static int FRAGMENT_SWITCHED = 6;

    @IntDef({SELECT_EXTERNALY, SELECT_BY_CLICK, SELECT_ONLY, ITEM_SELECTED, SWITCH_EXTERNALLY, FRAGMENT_SWITCHED})
    public @interface NavigationEventSenderType {
    }

    private LeftMenuSettingsData mSettingsData;
    private ArrayList<Integer> mStatesStack = new ArrayList<>();

    public WrappedNavigationData(LeftMenuSettingsData data, @NavigationEventSenderType int state) {
        mSettingsData = data;
        mStatesStack.add(state);
    }

    public ArrayList<Integer> getStatesStack() {
        return mStatesStack;
    }

    public WrappedNavigationData addStateToStack(@NavigationEventSenderType int state) {
        mStatesStack.add(state);
        return this;
    }

    public LeftMenuSettingsData getData() {
        return mSettingsData;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof WrappedNavigationData)) return false;
        WrappedNavigationData data = (WrappedNavigationData) o;
        return mSettingsData != null && mSettingsData.equals(data.getData())
                && mStatesStack.equals(data.getStatesStack());
    }

    @Override
    public int hashCode() {
        int res = mSettingsData != null ? mSettingsData.hashCode() : 0;
        return (res * 31) + mStatesStack.hashCode();
    }
}
