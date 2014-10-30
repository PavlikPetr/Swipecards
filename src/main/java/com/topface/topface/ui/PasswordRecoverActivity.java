package com.topface.topface.ui;

import android.os.Bundle;

import com.topface.topface.Static;
import com.topface.topface.ui.fragments.RecoverPwdFragment;

public class PasswordRecoverActivity extends NoAuthActivity<RecoverPwdFragment> {

    public static final int INTENT_RECOVER_PASSWORD = 5;

    @Override
    protected String getFragmentTag() {
        return RecoverPwdFragment.class.getSimpleName();
    }

    @Override
    protected RecoverPwdFragment createFragment() {
        String email = getIntent().getStringExtra(Static.EMAIL);
        Bundle arg = new Bundle();
        arg.putString(Static.EMAIL, email);
        RecoverPwdFragment recoverPwdFragment = new RecoverPwdFragment();
        recoverPwdFragment.setArguments(arg);
        return new RecoverPwdFragment();
    }
}
