package com.topface.topface.ui.edit;

import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import com.topface.topface.ui.fragments.BaseFragment;

public abstract class AbstractEditFragment extends BaseFragment {

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

            }
        });
    }

    protected void finishRequestSend() {
        if(getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mBackButton != null) {
                        mBackButton.setEnabled(true);
                    }

                    if (mRightPrsBar != null) {
                        mRightPrsBar.setVisibility(View.GONE);
                        if (!hasChanges()) {
                            mRightPrsBar.setVisibility(View.INVISIBLE);
                        }
                    }
                    unlockUi();
                    }
            });
        }
    }

    protected void refreshSaveState() {

    }

    protected abstract void lockUi();

    protected abstract void unlockUi();

    protected abstract boolean hasChanges();

    protected abstract void saveChanges(Handler handler);
}
