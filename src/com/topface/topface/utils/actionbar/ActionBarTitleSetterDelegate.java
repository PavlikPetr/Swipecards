package com.topface.topface.utils.actionbar;

import android.support.v7.app.ActionBar;

/**
 * Created by kirussell on 26.09.13.
 * Class
 */
public class ActionBarTitleSetterDelegate implements IActionBarTitleSetter{

    private ActionBar mActionBar;

    public ActionBarTitleSetterDelegate(ActionBar actionBar) {
        mActionBar = actionBar;
    }

    @Override
    public void setActionBarTitles(String title, String subtitle) {
        mActionBar.setTitle(title);
        mActionBar.setTitle(subtitle);
    }

    @Override
    public void setActionBarTitles(int title, int subtitle) {
        mActionBar.setTitle(title);
        mActionBar.setSubtitle(subtitle);
    }

    @Override
    public void setActionBarTitles(String title, int subtitle) {
        mActionBar.setTitle(title);
        mActionBar.setSubtitle(subtitle);
    }

    @Override
    public void setActionBarTitles(int title, String subtitle) {
        mActionBar.setTitle(title);
        mActionBar.setSubtitle(subtitle);
    }
}
