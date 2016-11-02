package com.topface.topface.ui;

import com.topface.topface.R;
import com.topface.topface.databinding.AcFragmentFrameBinding;
import com.topface.topface.databinding.ToolbarBinding;
import com.topface.topface.ui.fragments.feed.blacklist.BlackListFragment;

import org.jetbrains.annotations.NotNull;

public class BlackListActivity extends SingleFragmentActivity<BlackListFragment,AcFragmentFrameBinding> {

    @Override
    protected String getFragmentTag() {
        return BlackListFragment.class.getSimpleName();
    }

    @Override
    protected BlackListFragment createFragment() {
        return new BlackListFragment();
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
