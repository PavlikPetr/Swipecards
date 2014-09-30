package com.topface.topface.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;

/**
 * Activity which doesn't need to be auth
 */
public abstract class NoAuthActivity<T extends Fragment> extends SingleFragmentActivity<T> {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
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
