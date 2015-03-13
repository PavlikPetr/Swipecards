package com.topface.topface.ui;

import com.topface.topface.ui.fragments.profile.OwnProfileFragment;
import com.topface.topface.ui.fragments.profile.UserProfileFragment;

public class OwnProfileActivity extends CheckAuthActivity<OwnProfileFragment> {

    @Override
    protected String getFragmentTag() {
        return UserProfileFragment.class.getSimpleName();
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
