package com.topface.topface.ui.edit;

import android.os.Handler;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.TextView;

import com.topface.topface.ui.fragments.BaseFragment;

public abstract class AbstractEditFragment extends BaseFragment {

    protected Button mBackButton;
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
                setSupportProgressBarIndeterminateVisibility(true);
            }
        });
    }

    protected void finishRequestSend() {
        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mBackButton != null) {
                        mBackButton.setEnabled(true);
                    }
                    setSupportProgressBarIndeterminateVisibility(false);
                    unlockUi();
                }
            });
        }
    }

    public TextView.OnEditorActionListener getOnDoneListener() {
        return new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((actionId == EditorInfo.IME_ACTION_DONE)) {
                    getActivity().finish();
                }
                return false;
            }
        };
    }

    protected void refreshSaveState() {

    }

    protected abstract void lockUi();

    protected abstract void unlockUi();

    protected abstract boolean hasChanges();

    protected abstract void saveChanges(Handler handler);
}
