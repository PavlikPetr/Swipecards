package com.topface.topface.ui.edit;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.Toast;
import android.widget.TextView;

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

    protected void completeFailedRequest() {
        Activity activity = getActivity();
        if (activity != null) {
            activity.setResult(Activity.RESULT_CANCELED);
            Toast toast = Toast.makeText(getActivity(), R.string.profile_update_error, Toast.LENGTH_SHORT);
            toast.show();
        }
        finishRequestSend();
    }

    protected void warnEditingFailed(final Handler handler) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.general_error));
        builder.setMessage(R.string.retry_cancel_editing);
        builder.setNegativeButton(R.string.general_exit, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                completeFailedRequest();
                Activity activity = getActivity();
                if (handler == null && activity != null) {
                    activity.finish();
                } else {
                    handler.sendEmptyMessage(0);
                }
            }
        });
        builder.setPositiveButton(R.string.general_dialog_retry, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (handler != null) {
                    saveChanges(handler);
                }
            }
        });
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                finishRequestSend();
            }
        });
        builder.create().show();
    }
}
