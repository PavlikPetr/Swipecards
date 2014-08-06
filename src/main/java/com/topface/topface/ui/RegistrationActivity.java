package com.topface.topface.ui;

import com.topface.topface.ui.fragments.RegistrationFragment;

public class RegistrationActivity extends NoAuthActivity<RegistrationFragment> {

    public static final int INTENT_REGISTRATION = 4;

    @Override
    protected String getFragmentTag() {
        return RegistrationFragment.class.getSimpleName();
    }

    @Override
    protected RegistrationFragment createFragment() {
        return new RegistrationFragment();
    }
}
