package com.topface.topface.ui.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;

import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.utils.FormItem;

/**
 * Dialog for editing text form items
 */
public class EditTextFormDialog extends AbstractEditDialog<FormItem> {

    public static EditTextFormDialog newInstance(String title, FormItem notification,
                                                 final AbstractEditDialog.EditingFinishedListener<FormItem> editingFinishedListener) {
        final EditTextFormDialog editor = new EditTextFormDialog();
        Bundle args = new Bundle();
        args.putString(DIALOG_TITLE, title);
        args.putParcelable(DATA, notification);
        editor.setArguments(args);
        editor.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                editingFinishedListener.onEditingFinished(editor.getAdapter().getData());
            }
        });
        return editor;
    }
}
