package com.topface.topface.ui.bonus.view;

import android.content.Intent;

import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.databinding.AcFragmentFrameBinding;
import com.topface.topface.databinding.ToolbarViewBinding;
import com.topface.topface.ui.SingleFragmentActivity;

import org.jetbrains.annotations.NotNull;

public class BonusActivity extends SingleFragmentActivity<BonusFragment, AcFragmentFrameBinding> {

    @Override
    protected String getFragmentTag() {
        return BonusFragment.class.getSimpleName();
    }

    @Override
    protected BonusFragment createFragment() {
        return new BonusFragment().newInstance(true);
    }

    public static Intent createIntent() {
        return new Intent(App.getContext(), BonusActivity.class);
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
}
