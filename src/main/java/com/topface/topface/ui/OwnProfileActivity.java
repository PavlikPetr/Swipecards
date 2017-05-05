package com.topface.topface.ui;

import com.topface.topface.R;
import com.topface.topface.databinding.AcFragmentFrameBinding;
import com.topface.topface.databinding.ToolbarViewBinding;
import com.topface.topface.ui.fragments.profile.OwnProfileFragment;

import org.jetbrains.annotations.NotNull;

public class OwnProfileActivity extends CheckAuthActivity<OwnProfileFragment, AcFragmentFrameBinding> {

    @Override
    protected String getFragmentTag() {
        return OwnProfileFragment.class.getSimpleName();
    }

    @Override
    protected OwnProfileFragment createFragment() {
        return new OwnProfileFragment();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }

    @Override
    public void onBackPressed() {
        if (getBackPressedListener() == null || !getBackPressedListener().onBackPressed()) {
            super.onBackPressed();
        }
    }

    @NotNull
    @Override
    public ToolbarViewBinding getToolbarBinding(@NotNull AcFragmentFrameBinding binding) {
        return binding.toolbarInclude;
    }

    @Override
    public int getLayout() {
        return R.layout.ac_fragment_frame;
    }

    @Override
    public int getTabLayoutResId() {
        return R.id.toolbarInternalTabs;
    }
}