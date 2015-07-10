package com.topface.topface.ui;

import android.os.Bundle;

import com.topface.topface.ui.fragments.RecoverPwdFragment;
import com.topface.topface.ui.fragments.TopfaceLoginFragment;

public class RegistrationActivity extends NoAuthActivity<TopfaceLoginFragment> {

    public static final int INTENT_REGISTRATION = 4;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().show();
        }
    }

    @Override
    protected String getFragmentTag() {
        return TopfaceLoginFragment.class.getSimpleName();
    }

    @Override
    protected TopfaceLoginFragment createFragment() {
        TopfaceLoginFragment topfaceLoginFragment = new TopfaceLoginFragment();
        Bundle arg = new Bundle();
        arg.putString(RecoverPwdFragment.ARG_EMAIL, getIntent().getStringExtra(RecoverPwdFragment.ARG_EMAIL));
        topfaceLoginFragment.setArguments(arg);
        return topfaceLoginFragment;
    }
}
