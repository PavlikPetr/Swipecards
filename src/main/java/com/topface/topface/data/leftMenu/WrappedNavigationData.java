package com.topface.topface.data.leftMenu;

import android.support.annotation.IntDef;

/**
 * Created by ppavlik on 16.05.16.
 * Wrap LeftMenuSettingsData with added sender type to object
 */
public class WrappedNavigationData {

    public final static int SELECTED_EXTERNALY = 1;
    public final static int SELECTED_BY_CLICK = 2;
    public final static int SELECTED_BY_SWITCHER = 3;
    public final static int SWITCHED_BY_SELECTOR = 4;
    public final static int SWITCHED_EXTERNALY = 5;

    @IntDef({SELECTED_EXTERNALY, SELECTED_BY_CLICK, SELECTED_BY_SWITCHER, SWITCHED_BY_SELECTOR, SWITCHED_EXTERNALY})
    public @interface NavigationEventSenderType {
    }

    public final static int SELECTED_ITEM = 1;
    public final static int SWITCHED_FRAGMENT = 2;

    @IntDef({SELECTED_ITEM, SWITCHED_FRAGMENT})
    public @interface ActionType {
    }

    private LeftMenuSettingsData mSettingsData;
    @NavigationEventSenderType
    private int mSenderType;
    @ActionType
    private int mDataType;

    public WrappedNavigationData(LeftMenuSettingsData data, @NavigationEventSenderType int senderType, @ActionType int dataType) {
        mSettingsData = data;
        mSenderType = senderType;
        mDataType = dataType;
    }

    @ActionType
    public int getDataType() {
        return mDataType;
    }

    public LeftMenuSettingsData getData() {
        return mSettingsData;
    }

    @NavigationEventSenderType
    public int getSenderType() {
        return mSenderType;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof WrappedNavigationData)) return false;
        WrappedNavigationData data = (WrappedNavigationData) o;
        return mSettingsData != null && mSettingsData.equals(data.getData())
                && mSenderType == data.getSenderType()
                && mDataType == data.getDataType();
    }

    @Override
    public int hashCode() {
        int res = mSettingsData != null ? mSettingsData.hashCode() : 0;
        res = (res * 31) + mSenderType;
        return (res * 31) + mDataType;
    }
}
