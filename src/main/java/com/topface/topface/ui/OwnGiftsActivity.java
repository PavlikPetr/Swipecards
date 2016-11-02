package com.topface.topface.ui;

import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.databinding.AcFragmentFrameBinding;
import com.topface.topface.databinding.ToolbarBinding;
import com.topface.topface.ui.fragments.gift.OwnGiftsFragment;

import org.jetbrains.annotations.NotNull;

/**
 * Created by saharuk on 06.04.15.
 * Activity to display gifts
 */
public class OwnGiftsActivity extends CheckAuthActivity<OwnGiftsFragment, AcFragmentFrameBinding> {
    @Override
    protected String getFragmentTag() {
        return OwnGiftsFragment.class.getName();
    }

    @Override
    protected OwnGiftsFragment createFragment() {
        OwnGiftsFragment ownGiftsFragment = new OwnGiftsFragment();
        ownGiftsFragment.setProfile(App.from(this).getProfile());
        return ownGiftsFragment;
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
