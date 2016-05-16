package com.topface.topface.ui;

import com.topface.topface.data.leftMenu.LeftMenuSettingsData;

/**
 * Created by kirussell on 19.03.14.
 * Listener for events from child fargment to NavigationActivity
 */
public interface INavigationFragmentsListener {
    void onFragmentSwitch(LeftMenuSettingsData fragment);

    void onHideActionBar();

    void onShowActionBar();
}
