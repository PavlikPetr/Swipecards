package com.topface.topface.ui;

import com.topface.topface.ui.fragments.profile.OwnProfileFragment;

public class OwnProfileActivity extends CheckAuthActivity<OwnProfileFragment> {

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
}