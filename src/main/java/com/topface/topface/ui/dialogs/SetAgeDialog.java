package com.topface.topface.ui.dialogs;

import android.app.Dialog;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.widget.Button;

import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.requests.ApiRequest;
import com.topface.topface.requests.MultipartApiRequest;
import com.topface.topface.requests.ParallelApiRequest;
import com.topface.topface.requests.ProfileRequest;
import com.topface.topface.requests.SettingsRequest;
import com.topface.topface.ui.fragments.profile.ProfileFormListAdapter;
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
        return true;
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
        BaseEditDialog.EditingFinishedListener<FormItem> formEditedListener = new BaseEditDialog.EditingFinishedListener<FormItem>() {
            @Override
            public void onEditingFinished(final FormItem data) {
                item.copy(data);
                final SettingsRequest request = new SettingsRequest(getActivity());
                request.age = Integer.valueOf(data.value);
                ApiRequest updateAgeProfileRequest = new ParallelApiRequest(getActivity())
                        .addRequest(request)
                        .addRequest(new ProfileRequest(ProfileRequest.P_ALL, App.getContext()))
                        .setFrom(getClass().getSimpleName());
                updateAgeProfileRequest.exec();
            }
        };
        EditTextFormDialog.newInstance(item.getTitle(), item, formEditedListener).show(fm, EditTextFormDialog.class.getName());
    }

    private void closeDialog() {
        final Dialog dialog = getDialog();
        if (dialog != null) dismiss();
    }
}
