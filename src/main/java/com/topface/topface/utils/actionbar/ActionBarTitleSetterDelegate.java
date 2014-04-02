package com.topface.topface.utils.actionbar;

import android.support.v7.app.ActionBar;

/**
 * Created by kirussell on 26.09.13.
 * Class
 */
public class ActionBarTitleSetterDelegate implements IActionBarTitleSetter {

    private ActionBar mActionBar;
    private boolean mNoActionBar;

    public ActionBarTitleSetterDelegate(ActionBar actionBar) {
        mActionBar = actionBar;
        mNoActionBar = (mActionBar == null);
    }

    @Override
    public void setActionBarTitles(String title, String subtitle) {
        if (mNoActionBar) return;
        mActionBar.setTitle(title);
        mActionBar.setSubtitle(subtitle);
    }

    @Override
    public void setActionBarTitles(int title, int subtitle) {
        if (mNoActionBar) return;
        mActionBar.setTitle(title);
        mActionBar.setSubtitle(subtitle);
    }

    @Override
    public void setActionBarTitles(String title, int subtitle) {
        if (mNoActionBar) return;
        mActionBar.setTitle(title);
        mActionBar.setSubtitle(subtitle);
    }

    @Override
    public void setActionBarTitles(int title, String subtitle) {
        if (mNoActionBar) return;
        mActionBar.setTitle(title);
        mActionBar.setSubtitle(subtitle);
    }
}
