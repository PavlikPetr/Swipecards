package com.topface.topface.ui;

import android.os.Bundle;

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
        arg.putString(RecoverPwdFragment.ARG_EMAIL, getIntent().getStringExtra(RecoverPwdFragment.ARG_EMAIL));
        RecoverPwdFragment recoverPwdFragment = new RecoverPwdFragment();
        recoverPwdFragment.setArguments(arg);
        return recoverPwdFragment;
    }
}
