package com.topface.topface.ui;

import android.support.v4.app.Fragment;

import com.topface.topface.ui.fragments.RegistrationFragment;

public class RegistrationActivity extends NoAuthActivity {

    public static final int INTENT_REGISTRATION = 4;

    @Override
    protected Fragment createFragment() {
        return new RegistrationFragment();
    }
}
