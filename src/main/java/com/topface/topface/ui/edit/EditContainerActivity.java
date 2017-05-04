package com.topface.topface.ui.edit;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;

import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.databinding.AcFragmentFrameBinding;
import com.topface.topface.databinding.ToolbarViewBinding;
import com.topface.topface.ui.BaseFragmentActivity;
import com.topface.topface.ui.edit.filter.view.DatingFilterFragment;

import org.jetbrains.annotations.NotNull;

public class EditContainerActivity extends BaseFragmentActivity<AcFragmentFrameBinding> {

    public static final int INTENT_EDIT_FILTER = 201;

    Handler mFinishHandler = new Handler() {
        public void handleMessage(Message msg) {
            EditContainerActivity.super.finish();
        }
    };
    private Fragment mFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        switch (intent.getIntExtra(App.INTENT_REQUEST_KEY, 0)) {
            case INTENT_EDIT_FILTER:
                // хак чтобы тень под тулбаром была на том же фоне, что и фрагмент, при этом с возможностью свитчить
                // для разных фрагментов
                getViewBinding().getRoot().setBackgroundColor(getResources().getColor(R.color.gray_bg));
                mFragment = new DatingFilterFragment();
                break;
            default:
                break;
        }

        Fragment fragmentByTag = getSupportFragmentManager().findFragmentByTag(DatingFilterFragment.TAG);
        if (fragmentByTag != null) {
            mFragment = fragmentByTag;
        }
        if (mFragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_content, mFragment, DatingFilterFragment.TAG).commit();
        }
    }

    @Override
    public void finish() {
        if (mFragment instanceof AbstractEditFragment) {
            ((AbstractEditFragment) mFragment).saveChanges(mFinishHandler);
        } else {
            super.finish();
        }
    }

    @Override
    public void onUpClick() {
        super.finish();
    }

    @Override
    public boolean isTrackable() {
        return false;
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
