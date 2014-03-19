package com.topface.topface.ui;

import com.topface.topface.ui.fragments.BaseFragment;

/**
 * Created by kirussell on 19.03.14.
 * Listener for events from child fargment to NavigationActivity
 */
public interface INavigationFragmentsListener {
    public void onFragmentSwitch(BaseFragment.FragmentId fragment);

    public void onHideActionBar();

    public void onShowActionBar();
}
