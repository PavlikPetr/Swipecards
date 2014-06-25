package com.topface.topface.ui;

/**
 * Activity which doesn't need to be auth
 */
public class NoAuthActivity extends BaseFragmentActivity {

    @Override
    protected boolean isNeedAuth() {
        return false;
    }

    @Override
    public boolean isTrackable() {
        return false;
    }
}
