package com.topface.topface.ui;

import android.view.View;

/**
 * Activity which doesn't need to be auth
 */
public abstract class NoAuthActivity extends SingleFragmentActivity {

    @Override
    protected boolean isNeedAuth() {
        return false;
    }

    @Override
    public boolean isTrackable() {
        return false;
    }

    @Override
    protected void initCustomActionBarView(View mCustomView) {

    }

    @Override
    protected int getActionBarCustomViewResId() {
        return 0;
    }
}
