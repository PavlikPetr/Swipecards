package com.topface.topface.ui.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.topface.topface.R;
import com.topface.topface.ui.IDialogListener;
import com.topface.topface.ui.analytics.TrackedDialogFragment;

/**
 * Created by ppetr on 17.08.15.
 * Show dialog about old version App
 */
public class OldVersionDialog extends TrackedDialogFragment {

    private final static String IS_DIALOG_CANCELABLE = "is_dialog_cancelable";

    private IDialogListener mIDialogListener;

    public static OldVersionDialog newInstance(boolean cancelable) {
        OldVersionDialog oldVersionDialog = new OldVersionDialog();
        Bundle bundle = new Bundle();
        bundle.putBoolean(IS_DIALOG_CANCELABLE, cancelable);
        oldVersionDialog.setArguments(bundle);
        return oldVersionDialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        boolean isCancelable = false;
        Bundle bundle = getArguments();
        if (bundle != null) {
            isCancelable = bundle.getBoolean(IS_DIALOG_CANCELABLE);
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setPositiveButton(R.string.popup_version_update, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (mIDialogListener != null) {
                    mIDialogListener.onPositiveButtonClick();
                }
            }
        });
        if (isCancelable) {
            builder.setNegativeButton(R.string.popup_version_cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (mIDialogListener != null) {
                        mIDialogListener.onNegativeButtonClick();
                    }
                }
            });
        }
        builder.setMessage(R.string.general_version_not_supported);
        builder.setCancelable(isCancelable);
        return builder.create();
    }

    public void setDialogInterface(IDialogListener dialogInterface) {
        mIDialogListener = dialogInterface;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (mIDialogListener != null) {
            mIDialogListener.onDismissListener();
        }
    }
}
