package com.topface.topface.ui;

import com.topface.topface.ui.fragments.RecoverPwdFragment;

public class PasswordRecoverActivity extends NoAuthActivity<RecoverPwdFragment> {

    public static final int INTENT_RECOVER_PASSWORD = 5;

    @Override
    protected String getFragmentTag() {
        return RecoverPwdFragment.class.getSimpleName();
    }

    @Override
    protected RecoverPwdFragment createFragment() {
        return new RecoverPwdFragment();
    }
}
