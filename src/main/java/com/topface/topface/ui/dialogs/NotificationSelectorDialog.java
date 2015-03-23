package com.topface.topface.ui.dialogs;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.topface.topface.R;
import com.topface.topface.data.Profile;
import com.topface.topface.ui.adapters.NotificationSelectorAdapter;
import com.topface.topface.utils.CacheProfile;

/**
 * Dialog for editing notification options
 */
public class NotificationSelectorDialog extends AbstractSelectorDialog<Profile.TopfaceNotifications> {

    public static NotificationSelectorDialog newInstance(String title, Profile.TopfaceNotifications notification,
                                             final AbstractSelectorDialog.EditingFinishedListener<Profile.TopfaceNotifications> editingFinishedListener) {
        final NotificationSelectorDialog selector = new NotificationSelectorDialog();
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
