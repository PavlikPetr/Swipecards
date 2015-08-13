package com.topface.topface.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;

/**
 * Activity which doesn't need to be auth
 */
public abstract class NoAuthActivity<T extends Fragment> extends SingleFragmentActivity<T> {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            if (isNeedShowActionBar()) {
                ab.show();
            } else {
                ab.hide();
            }
        }
    }

    protected boolean isNeedShowActionBar() {
        return false;
    }

    @Override
    protected boolean isNeedAuth() {
        return false;
    }

    @Override
    public boolean isTrackable() {
        return false;
    }
}
