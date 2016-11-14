package com.topface.topface.ui;

import android.app.Activity;
import android.content.Intent;

import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.databinding.AcFragmentFrameBinding;
import com.topface.topface.databinding.ToolbarBinding;
import com.topface.topface.ui.fragments.SmsInviteFragment;
import com.topface.topface.ui.views.toolbar.view_models.BackToolbarViewModel;
import com.topface.topface.ui.views.toolbar.view_models.BaseToolbarViewModel;

import org.jetbrains.annotations.NotNull;

public class SMSInviteActivity extends CheckAuthActivity<SmsInviteFragment, AcFragmentFrameBinding> {

    public static final int INTENT_CONTACTS = 8;

    public static Intent createIntent(Activity context) {
        Intent intent = new Intent(context, SMSInviteActivity.class);
        intent.putExtra(App.INTENT_REQUEST_KEY, INTENT_CONTACTS);
        return intent;
    }

    @Override
    protected String getFragmentTag() {
        return SmsInviteFragment.class.getSimpleName();
    }

    @Override
    protected SmsInviteFragment createFragment() {
        return new SmsInviteFragment();
    }

    @NotNull
    @Override
    protected BaseToolbarViewModel generateToolbarViewModel(@NotNull ToolbarBinding toolbar) {
        return new BackToolbarViewModel(toolbar, getString(R.string.sms_invite_title), this);
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
