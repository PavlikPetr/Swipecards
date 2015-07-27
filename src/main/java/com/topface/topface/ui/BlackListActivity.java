package com.topface.topface.ui;

import android.support.v4.app.Fragment;

import com.topface.topface.ui.fragments.feed.BlackListFragment;


public class BlackListActivity extends SingleFragmentActivity {

    @Override
    protected String getFragmentTag() {
        return BlackListFragment.class.getSimpleName();
    }

    @Override
    protected Fragment createFragment() {
        return new BlackListFragment();
    }
}
