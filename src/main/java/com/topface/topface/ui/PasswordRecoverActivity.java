package com.topface.topface.ui;

import android.support.v4.app.Fragment;

import com.topface.topface.ui.fragments.RecoverPwdFragment;

public class PasswordRecoverActivity extends NoAuthActivity {

    public static final int INTENT_RECOVER_PASSWORD = 5;

    @Override
    protected Fragment createFragment() {
        return new RecoverPwdFragment();
    }
}
