package com.topface.topface.ui.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.ProfileDeleteRequest;
import com.topface.topface.requests.handlers.ApiHandler;
import com.topface.topface.ui.views.ImageViewRemote;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.social.AuthToken;
import com.topface.topface.utils.social.AuthorizationManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DeleteAccountDialog extends DialogFragment implements View.OnClickListener {

    public static final String TAG = "com.topface.topface.ui.dialogs.DeleteAccountDialog_TAG";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.dialog_delete_account, container, false);

        setTransparentBackground();
        getDialog().setCanceledOnTouchOutside(false);

        root.findViewById(R.id.btnClose).setOnClickListener(this);
        root.findViewById(R.id.btnCancel).setOnClickListener(this);
        root.findViewById(R.id.btnOk).setOnClickListener(this);
        ((ImageViewRemote) root.findViewById(R.id.ivAvatar)).setPhoto(CacheProfile.photo);
        ((TextView) root.findViewById(R.id.tvProfile)).setText(CacheProfile.getUserNameAgeString());

        ((TextView) root.findViewById(R.id.tvWarningText)).setText(R.string.delete_account_warning);

        return root;
    }

    private void setTransparentBackground() {
        ColorDrawable color = new ColorDrawable(Color.BLACK);
        color.setAlpha(175);
        getDialog().getWindow().setBackgroundDrawable(color);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnClose:
            case R.id.btnCancel:
                closeDialog();
                break;
            case R.id.btnOk:
                new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.settings_delete_account)
                        .setMessage(R.string.delete_account_are_you_sure)
                        .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                final ProgressDialog progress = new ProgressDialog(getActivity());
                                progress.setTitle(R.string.settings_delete_account);
                                progress.setMessage(getActivity().getResources().getString(R.string.settings_delete_account));
                                progress.show();

                                ProfileDeleteRequest request = new ProfileDeleteRequest(5,
                                        "From Android Device", getActivity());
                                request.callback(new ApiHandler() {
                                    @Override
                                    public void success(IApiResponse response) {
                                        if (response.isCompleted()) {
                                            AuthorizationManager.logout(getActivity());
                                        } else {
                                            fail(response.getResultCode(), response);
                                        }
                                    }

                                    @Override
                                    public void fail(int codeError, IApiResponse response) {
                                        Toast.makeText(App.getContext(), R.string.delete_account_error, Toast.LENGTH_SHORT)
                                                .show();
                                    }

                                    @Override
                                    public void always(IApiResponse response) {
                                        super.always(response);
                                        progress.dismiss();
                                    }
                                }).exec();
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                closeDialog();
                            }
                        }).show();
                break;
        }
    }

    private void closeDialog() {
        final Dialog dialog = getDialog();
        if (dialog != null) dialog.dismiss();
    }

    public static DeleteAccountDialog newInstance() {
        DeleteAccountDialog dialog = new DeleteAccountDialog();
        dialog.setStyle(DialogFragment.STYLE_NO_TITLE, R.style.Theme_Topface);
        return dialog;
    }
}
