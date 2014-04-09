package com.topface.topface.ui.edit;

import android.app.Activity;
import android.os.Handler;
import android.widget.Button;
import android.widget.Toast;

import com.topface.topface.R;
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

    protected void refreshSaveState() {

    }

    protected abstract void lockUi();

    protected abstract void unlockUi();

    protected abstract boolean hasChanges();

    protected abstract void saveChanges(Handler handler);

    protected void completeFailedRequest() {
        getActivity().setResult(Activity.RESULT_CANCELED);
        Toast toast = Toast.makeText(getActivity(), R.string.profile_update_error, Toast.LENGTH_SHORT);
        toast.show();
        finishRequestSend();
    }
}
