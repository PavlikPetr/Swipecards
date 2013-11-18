package com.topface.topface.ui.fragments;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;

import com.topface.topface.R;
import com.topface.topface.ui.analytics.TrackedDialogFragment;

public class ProgressDialogFragment extends TrackedDialogFragment {
    public final static String PROGRESS_DIALOG_TAG = "progress_dialog";

    public static ProgressDialogFragment newInstance() {
        return new ProgressDialogFragment();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final ProgressDialog dialog = new ProgressDialog(getActivity()) {
            @Override
            public void onBackPressed() {
            }
        };
        dialog.setMessage(getString(R.string.general_dialog_loading));
        dialog.setIndeterminate(true);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }

    @Override
    public boolean isTrackable() {
        return false;
    }
}
