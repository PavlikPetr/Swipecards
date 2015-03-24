package com.topface.topface.ui.dialogs;

import android.content.DialogInterface;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import com.topface.topface.utils.FormItem;

/**
 * Adapter for editing forms with choice
 */
public class EditFormItemsSelectorDialog extends AbstractSelectorDialog<FormItem> {

    public static final int WINDOW_CLOSING_DELAY = 100;

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

    @Override
    protected void initViews(View root) {
        super.initViews(root);
        getAdapter().registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        EditFormItemsSelectorDialog.this.dismiss();
                    }
                }, WINDOW_CLOSING_DELAY
                );
            }
        });
    }
}
