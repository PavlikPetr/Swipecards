package com.topface.topface.ui;

import com.ifree.vendors.updateversion.Updater;

/**
 * Created by ppetr on 20.07.15.
 * Start/Stop working i-Free updater
 */
public abstract class ParrentNavigationActivity extends BaseFragmentActivity {

    @Override
    protected int getContentLayout() {
        return getContentLayoutId();
    }

    protected abstract int getContentLayoutId();

    @Override
    public void onStart() {
        super.onStart();
        Updater.actionStart(this.getApplicationContext());
    }

    @Override
    public void onStop() {
        super.onStop();
        Updater.actionStop();
    }
}