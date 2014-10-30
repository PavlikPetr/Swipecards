package com.topface.topface.ui;

import android.os.Bundle;

import com.topface.topface.ui.fragments.RecoverPwdFragment;
import com.topface.topface.ui.fragments.RegistrationFragment;

public class RegistrationActivity extends NoAuthActivity<RegistrationFragment> {

    public static final int INTENT_REGISTRATION = 4;

    @Override
    protected String getFragmentTag() {
        return RegistrationFragment.class.getSimpleName();
    }

    @Override
    protected RegistrationFragment createFragment() {
        RegistrationFragment registrationFragment = new RegistrationFragment();
        Bundle arg = new Bundle();
        arg.putString(RecoverPwdFragment.ARG_EMAIL, getIntent().getStringExtra(RecoverPwdFragment.ARG_EMAIL));
        registrationFragment.setArguments(arg);
        return registrationFragment;
    }
}
