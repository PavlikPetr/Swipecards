package com.topface.topface.ui;

import com.topface.topface.data.FragmentSettings;

/**
 * Created by kirussell on 19.03.14.
 * Listener for events from child fargment to NavigationActivity
 */
public interface INavigationFragmentsListener {
    void onFragmentSwitch(FragmentSettings fragment);

    void onHideActionBar();

    void onShowActionBar();
}
