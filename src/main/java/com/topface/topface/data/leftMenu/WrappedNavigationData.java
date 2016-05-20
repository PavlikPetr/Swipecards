package com.topface.topface.data.leftMenu;

import android.support.annotation.IntDef;

/**
 * Created by ppavlik on 16.05.16.
 * Wrap LeftMenuSettingsData with added sender type to object
 */
public class WrappedNavigationData {

    public final static int SELECTED_BY_CLICK = 1;
    public final static int SELECTED_BY_SWITCHER = 2;
    public final static int SWITCHED_BY_SELECTOR = 3;
    public final static int SWITCHED_EXTERNALY = 4;

    @IntDef({SELECTED_BY_CLICK, SELECTED_BY_SWITCHER, SWITCHED_BY_SELECTOR, SWITCHED_EXTERNALY})
    public @interface NavigationEventSenderType {
    }

    private LeftMenuSettingsData mSettingsData;
    @NavigationEventSenderType
    private int mSenderType;

    public WrappedNavigationData(LeftMenuSettingsData data, @NavigationEventSenderType int senderType) {
        mSettingsData = data;
        mSenderType = senderType;
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
        return mSettingsData != null && mSettingsData.equals(data.getData()) && mSenderType == data.getSenderType();
    }

    @Override
    public int hashCode() {
        int res = mSettingsData != null ? mSettingsData.hashCode() : 0;
        return (res * 31) + mSenderType;
    }
}
