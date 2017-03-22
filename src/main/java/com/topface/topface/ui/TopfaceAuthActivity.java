package com.topface.topface.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.topface.topface.R;
import com.topface.topface.databinding.AcFragmentFrameBinding;
import com.topface.topface.databinding.ToolbarBinding;
import com.topface.topface.ui.fragments.TopfaceAuthFragment;

import org.jetbrains.annotations.NotNull;

/**
 * Activity for Topface authorization
 */
public class TopfaceAuthActivity extends NoAuthActivity<TopfaceAuthFragment, AcFragmentFrameBinding> {

    public static final int INTENT_TOPFACE_AUTH = 26;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    @Override
    protected boolean isNeedShowActionBar() {
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    @Override
    protected String getFragmentTag() {
        return TopfaceAuthFragment.class.getSimpleName();
    }

    @Override
    protected TopfaceAuthFragment createFragment() {
        return new TopfaceAuthFragment();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && requestCode == RestoreAccountActivity.RESTORE_RESULT) {
            finish();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @NotNull
    @Override
    public ToolbarBinding getToolbarBinding(@NotNull AcFragmentFrameBinding binding) {
        return binding.toolbarInclude;
    }

    @Override
    public int getLayout() {
        return R.layout.ac_fragment_frame;
    }
}
