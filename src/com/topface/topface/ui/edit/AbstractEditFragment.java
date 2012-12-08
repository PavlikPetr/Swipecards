package com.topface.topface.ui.edit;

import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import com.topface.topface.ui.fragments.BaseFragment;

public abstract class AbstractEditFragment extends BaseFragment {

    protected Button mSaveButton;
    protected Button mBackButton;
    protected ProgressBar mRightPrsBar;
    protected Handler mFinishHandler;

    public AbstractEditFragment() {
        super();
    }

    protected void prepareRequestSend() {
        getActivity().runOnUiThread(new Runnable() {

            @Override
            public void run() {
                lockUi();
                if (mBackButton != null) {
                    mBackButton.setEnabled(false);
                }

                if (mRightPrsBar != null) {
                    mRightPrsBar.setVisibility(View.VISIBLE);
                }

                if (mSaveButton != null) {
                    mSaveButton.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    protected void finishRequestSend() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mBackButton != null) {
                    mBackButton.setEnabled(true);
                }

                if (mRightPrsBar != null) {
                    mRightPrsBar.setVisibility(View.GONE);
                    if (hasChanges()) {
                        if (mSaveButton != null) {
                            mSaveButton.setVisibility(View.VISIBLE);
                        }
                    } else {
                        mRightPrsBar.setVisibility(View.INVISIBLE);
                    }
                }
                unlockUi();
            }
        });
    }

    protected void refreshSaveState() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mSaveButton != null) {
                    if (hasChanges()) {
                        mSaveButton.setVisibility(View.VISIBLE);
                    } else {
                        mSaveButton.setVisibility(View.INVISIBLE);
                    }
                }
            }
        });
    }

    protected abstract void lockUi();

    protected abstract void unlockUi();

    protected abstract boolean hasChanges();

    protected abstract void saveChanges(Handler handler);
}
