package com.topface.topface.ui;

import android.os.Bundle;

import com.topface.topface.ui.fragments.AuthFragment;
import com.topface.topface.ui.fragments.RecoverPwdFragment;

public class PasswordRecoverActivity extends NoAuthActivity<RecoverPwdFragment> {

    public static final int INTENT_RECOVER_PASSWORD = 5;

    @Override
    protected String getFragmentTag() {
        return RecoverPwdFragment.class.getSimpleName();
    }

    @Override
    protected RecoverPwdFragment createFragment() {
        Bundle arg = new Bundle();
        arg.putString(AuthFragment.ARG_EMAIL, getIntent().getStringExtra(AuthFragment.ARG_EMAIL));
        RecoverPwdFragment recoverPwdFragment = new RecoverPwdFragment();
        recoverPwdFragment.setArguments(arg);
        return new RecoverPwdFragment();
    }
}
