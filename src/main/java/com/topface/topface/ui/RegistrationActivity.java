package com.topface.topface.ui;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.WindowManager;

import com.topface.topface.R;
import com.topface.topface.databinding.AcFragmentFrameBinding;
import com.topface.topface.databinding.ToolbarBinding;
import com.topface.topface.ui.fragments.RecoverPwdFragment;
import com.topface.topface.ui.fragments.RegistrationFragment;

import org.jetbrains.annotations.NotNull;

public class RegistrationActivity extends NoAuthActivity<RegistrationFragment, AcFragmentFrameBinding> {

    public static final int INTENT_REGISTRATION = 4;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        }
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.show();
        }
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    @Override
    protected String getFragmentTag() {
        return RegistrationFragment.class.getSimpleName();
    }

    @Override
    protected RegistrationFragment createFragment() {
        RegistrationFragment fragment = RegistrationFragment.getInstance();
        Bundle arg = new Bundle();
        arg.putString(RecoverPwdFragment.ARG_EMAIL, getIntent().getStringExtra(RecoverPwdFragment.ARG_EMAIL));
        fragment.setArguments(arg);
        return fragment;
    }

    @NotNull
    @Override
    public ToolbarBinding getToolbarBinding(@NotNull AcFragmentFrameBinding binding) {
        return binding.toolbar;
    }

    @Override
    public int getLayout() {
        return R.layout.ac_fragment_frame;
    }
}
