package com.topface.topface.ui.dialogs;

import android.content.DialogInterface;
import android.os.Bundle;

import com.topface.topface.utils.FormItem;

/**
 * Adapter for editing forms with choice
 */
public class EditFormItemsSelectorDialog extends AbstractSelectorDialog<FormItem> {

    public static EditFormItemsSelectorDialog newInstance(String title, FormItem notification,
                                                          final AbstractSelectorDialog.EditingFinishedListener<FormItem> editingFinishedListener) {
        final EditFormItemsSelectorDialog selector = new EditFormItemsSelectorDialog();
        Bundle selectorArgs = new Bundle();
        selectorArgs.putString(DIALOG_TITLE, title);
        selectorArgs.putParcelable(DATA, notification);
        selector.setArguments(selectorArgs);
        selector.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                editingFinishedListener.onEditingFinished(selector.getAdapter().getData());
            }
        });
        return selector;
    }
}
