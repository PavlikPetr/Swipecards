package com.topface.topface.ui.dialogs;

import android.content.DialogInterface;
import android.os.Bundle;

import com.topface.topface.data.Profile;

/**
 * Dialog for editing notification options
 */
public class NotificationEditDialog extends AbstractEditDialog<Profile.TopfaceNotifications> {

    public static NotificationEditDialog newInstance(String title, Profile.TopfaceNotifications notification,
                                                     final AbstractEditDialog.EditingFinishedListener<Profile.TopfaceNotifications> editingFinishedListener) {
        final NotificationEditDialog selector = new NotificationEditDialog();
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
