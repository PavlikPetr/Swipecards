package com.topface.topface.ui;

import android.app.Activity;
import android.content.Intent;

import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.databinding.AcFragmentFrameBinding;
import com.topface.topface.databinding.ToolbarViewBinding;
import com.topface.topface.ui.fragments.ComplainsFragment;

import org.jetbrains.annotations.NotNull;

public class ComplainsActivity extends CheckAuthActivity<ComplainsFragment, AcFragmentFrameBinding> {

    public static final int INTENT_COMPLAIN = 9;
    public static final int REQUEST_CODE = 312;

    public static Intent createIntent(int userId) {
        Intent intent = new Intent(App.getContext(), ComplainsActivity.class);
        intent.putExtra(App.INTENT_REQUEST_KEY, INTENT_COMPLAIN);
        intent.putExtra(ComplainsFragment.USERID, userId);
        return intent;
    }

    public static Intent createIntent(int userId, String feedId) {
        Intent intent = createIntent(userId);
        intent.putExtra(ComplainsFragment.FEEDID, feedId);
        return intent;
    }

    public static Intent createIntent(int userId, boolean isNeedResult) {
        Intent intent = createIntent(userId);
        intent.putExtra(ComplainsFragment.NEED_RESULT, isNeedResult);
        return intent;
    }

    @Override
    protected String getFragmentTag() {
        return ComplainsFragment.class.getSimpleName();
    }

    @Override
    protected ComplainsFragment createFragment() {
        return new ComplainsFragment();
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            setResult(Activity.RESULT_OK);
            finish();
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
