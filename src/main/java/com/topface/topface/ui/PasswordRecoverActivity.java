package com.topface.topface.ui;

import android.os.Bundle;
import android.view.WindowManager;

import com.topface.topface.R;
import com.topface.topface.databinding.AcFragmentFrameBinding;
import com.topface.topface.databinding.ToolbarBinding;
import com.topface.topface.ui.fragments.RecoverPwdFragment;

import org.jetbrains.annotations.NotNull;

public class PasswordRecoverActivity extends NoAuthActivity<RecoverPwdFragment, AcFragmentFrameBinding> {

    public static final int INTENT_RECOVER_PASSWORD = 5;

    @Override
    protected String getFragmentTag() {
        return RecoverPwdFragment.class.getSimpleName();
    }

    @Override
    protected RecoverPwdFragment createFragment() {
        return new RecoverPwdFragment();
    }

    @Override
    protected boolean isNeedShowActionBar() {
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        }
        super.onCreate(savedInstanceState);
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
