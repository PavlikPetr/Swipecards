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
 * Created by Петр on 15.03.2016.
 * Show dialog about old version App
 */
public class OldVersionDialog extends TrackedDialogFragment {

    private final static String IS_DIALOG_CANCELABLE = "is_dialog_cancelable";

    private IDialogListener mIDialogListener;
    boolean mIsCancelable = false;

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
        Bundle bundle = savedInstanceState != null ? savedInstanceState : getArguments() != null ? getArguments() : null;
        if (bundle != null) {
            mIsCancelable = bundle.getBoolean(IS_DIALOG_CANCELABLE);
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setPositiveButton(R.string.popup_version_update, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (mIDialogListener != null) {
                    mIDialogListener.onPositiveButtonClick();
                }
            }
        });
        if (mIsCancelable) {
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
        builder.setCancelable(mIsCancelable);
        return builder.create();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(IS_DIALOG_CANCELABLE, mIsCancelable);
    }

    public void setDialogInterface(IDialogListener dialogInterface) {
        mIDialogListener = dialogInterface;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        if (mIDialogListener != null) {
            mIDialogListener.onDismissListener();
        }
    }
}
