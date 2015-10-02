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

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }

    @Override
    public void onBackPressed() {
        if (getBackPressedListener() == null || !getBackPressedListener().onBackPressed()) {
            super.onBackPressed();
        }
    }
}