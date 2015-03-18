package com.topface.topface.ui.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.topface.topface.R;
import com.topface.topface.data.Profile;
import com.topface.topface.ui.adapters.NotificationSelectorAdapter;
import com.topface.topface.utils.CacheProfile;

/**
 * Created by saharuk on 17.03.15.
 */
public class SelectorDialog extends AbstractDialogFragment implements View.OnClickListener {

    public static final String DIALOG_TITLE = "dialog_title";
    public static final String NOTIFICATION_TYPE = "notification_type";

    public static interface EditingFinishedListener {

        public void onEditingFinished(Profile.TopfaceNotifications notification);
    }

    private String mTitle;
    private NotificationSelectorAdapter mAdapter;
    private Profile.TopfaceNotifications mNotification;

    public static SelectorDialog newInstance(String title, int notificationType,
                                             final EditingFinishedListener editingFinishedListener) {
        final SelectorDialog selectorDialog = new SelectorDialog();
        Bundle selectorArgs = new Bundle();
        selectorArgs.putString(DIALOG_TITLE, title);
        selectorArgs.putInt(NOTIFICATION_TYPE, notificationType);
        selectorDialog.setArguments(selectorArgs);
        selectorDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                editingFinishedListener.onEditingFinished(selectorDialog.mAdapter.getNotification());
            }
        });
        return selectorDialog;
    }

    @Override
    protected void applyStyle() {
        setStyle(ConfirmEmailDialog.STYLE_NO_TITLE, R.style.Selector);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Bundle args = getArguments();
        if (args != null) {
            mTitle = args.getString(DIALOG_TITLE);
            mNotification = CacheProfile.notifications.get(args.getInt(NOTIFICATION_TYPE));
            mAdapter = new NotificationSelectorAdapter(mNotification);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.settings_selector_dialog, container, false);
        initViews(view);
        return view;
    }

    @Override
    protected void initViews(View root) {
        ListView optionsList = (ListView) root.findViewById(R.id.optionsList);
        optionsList.setAdapter(mAdapter);
    }

    @Override
    public int getDialogLayoutRes() {
        return 0;
    }

    @Override
    public Dialog getDialog() {
        Dialog dialog = super.getDialog();
        dialog.setTitle(mTitle);
        return dialog;
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
    }
}
