package com.topface.topface.ui.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;

import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.ui.NotificationSelectorTypes;

import java.lang.ref.WeakReference;

/**
 * Created by saharuk on 05.03.15.
 */
public class NotificationSelector {

    private WeakReference<Context> mContextRef;

    public NotificationSelector(Context context) {
        mContextRef = new WeakReference<Context>(context);
        startSelection();
    }

    protected void startSelection() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(mContextRef.get()).
                setTitle(getTitle());
        setItems(dialogBuilder);
        dialogBuilder.show();
    }

    protected String getTitle() {
//        return App.getContext().getString(R.string.receive_sympathy_notification);
        return "";
    }

    protected void setItems(AlertDialog.Builder dialogBuilder) {
        NotificationSelectorTypes[] values = NotificationSelectorTypes.values();
        String[] items = new String[values.length];
        for (int i = 0; i < items.length; i++) {
            items[i] = App.getContext().getString(values[i].getName());
        }
        boolean[] checkedItems = new boolean[values.length];
        for (int i = 0; i < values.length; i++) {
            checkedItems[i] = false;
        }
        dialogBuilder.setMultiChoiceItems(items, checkedItems, null);
    }

//    @Override
//    public int getDialogLayoutRes() {
//        return R.layout.settings_selector_dialog;
//    }

//    @NonNull
//    @Override
//    public Dialog onCreateDialog(Bundle savedInstanceState) {
//        Dialog dialog = super.onCreateDialog(savedInstanceState);
//        dialog.setTitle(R.string.receive_sympathy_notification);
//        return dialog;
//    }
}
