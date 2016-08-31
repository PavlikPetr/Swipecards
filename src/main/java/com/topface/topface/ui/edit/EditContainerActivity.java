package com.topface.topface.ui.edit;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;

import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.ui.BaseFragmentActivity;
import com.topface.topface.ui.edit.filter.view.FilterFragment;

public class EditContainerActivity extends BaseFragmentActivity {

    public static final int INTENT_EDIT_FILTER = 201;

    Handler mFinishHandler = new Handler() {
        public void handleMessage(Message msg) {
            EditContainerActivity.super.finish();
        }
    };
    private Fragment mFragment;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        Intent intent = getIntent();
        switch (intent.getIntExtra(App.INTENT_REQUEST_KEY, 0)) {
            case INTENT_EDIT_FILTER:
                mFragment = new FilterFragment();
                break;
            default:
                break;
        }

        Fragment fragmentByTag = getSupportFragmentManager().findFragmentByTag(FilterFragment.TAG);
        if (fragmentByTag != null) {
            mFragment = fragmentByTag;
        }
        if (mFragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.loFrame, mFragment, FilterFragment.TAG).commit();
        }
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
    }

    @Override
    protected int getContentLayout() {
        return R.layout.ac_fragment_frame;
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
    public boolean isTrackable() {
        return false;
    }

}
