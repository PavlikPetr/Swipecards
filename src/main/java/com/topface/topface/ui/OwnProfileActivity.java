package com.topface.topface.ui;

import com.topface.topface.ui.fragments.profile.OwnProfileFragment;
import com.topface.topface.utils.IActivityDelegate;

public class OwnProfileActivity extends CheckAuthActivity<OwnProfileFragment> implements IActivityDelegate {

    @Override
    protected String getFragmentTag() {
        return OwnProfileFragment.class.getSimpleName();
    }

    @Override
    protected OwnProfileFragment createFragment() {
        return new OwnProfileFragment();
    }

    @Override
    protected void setActionBarView() {
        super.setActionBarView();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }
}