package com.topface.topface.ui.fragments;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import com.topface.topface.R;

public class ProgressDialogFragment extends DialogFragment {
    public final static String PROGRESS_DIALOG_TAG = "progress_dialog";

    public static ProgressDialogFragment newInstance() {
        ProgressDialogFragment fragment = new ProgressDialogFragment();
        return fragment;
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
}
