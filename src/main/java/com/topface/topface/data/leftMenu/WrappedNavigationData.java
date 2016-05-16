package com.topface.topface.data.leftMenu;

import android.support.annotation.IntDef;

/**
 * Created by ppavlik on 16.05.16.
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
}
