package com.topface.topface.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;

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

    /**
     * Empty initialization because no action bar needed here.
     */
    @Override
    protected void initCustomActionBarView(View mCustomView) {
        // Empty initialization because no action bar needed here
    }

    /**
     * 0 resource id because no action bar needed here.
     */
    @Override
    protected int getActionBarCustomViewResId() {
        return 0;
    }
}
