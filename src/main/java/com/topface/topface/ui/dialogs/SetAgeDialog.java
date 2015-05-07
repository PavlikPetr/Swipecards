package com.topface.topface.ui.dialogs;

import android.app.Dialog;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.widget.Button;

import com.topface.topface.R;
import com.topface.topface.ui.fragments.profile.ProfileFormListAdapter;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.FormItem;


public class SetAgeDialog extends AbstractDialogFragment implements View.OnClickListener {

    public static final String TAG = "SETUP_AGE_DIALOG";

    public static SetAgeDialog newInstance() {
        final SetAgeDialog dialog = new SetAgeDialog();
        return dialog;
    }

    @Override
    protected void initViews(View root) {
        Button setBirthdayButton = (Button) root.findViewById(R.id.setBirthday);
        setBirthdayButton.setOnClickListener(this);
    }

    @Override
    protected boolean isModalDialog() {
        return false;
    }

    @Override
    protected int getDialogLayoutRes() {
        return R.layout.birthday_confirm_popup;
    }

    @Override
    public void onClick(View v) {
        closeDialog();
        FragmentManager fm = getFragmentManager();
        final FormItem item = ProfileFormListAdapter.getAgeItem();
        AbstractEditDialog.EditingFinishedListener<FormItem> formEditedListener = new AbstractEditDialog.EditingFinishedListener<FormItem>() {
            @Override
            public void onEditingFinished(FormItem data) {
                item.copy(data);
                CacheProfile.sendUpdateProfileBroadcast();
            }
        };
        EditTextFormDialog.newInstance(item.getTitle(), item, formEditedListener).show(fm, EditTextFormDialog.class.getName());

    }

    private void closeDialog() {
        final Dialog dialog = getDialog();
        if (dialog != null) dismiss();
    }

}
