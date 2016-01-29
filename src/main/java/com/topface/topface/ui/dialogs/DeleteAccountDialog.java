package com.topface.topface.ui.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.data.Profile;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.ProfileDeleteRequest;
import com.topface.topface.requests.handlers.ApiHandler;
import com.topface.topface.ui.views.ImageViewRemote;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Utils;
import com.topface.topface.utils.social.AuthorizationManager;

public class DeleteAccountDialog extends AbstractDialogFragment implements View.OnClickListener {

    public static final String TAG = "com.topface.topface.ui.dialogs.DeleteAccountDialog_TAG";
    private Button mBtnOk;

    @Override
    protected void initViews(View root) {
        root.findViewById(R.id.btnCancel).setOnClickListener(this);
        mBtnOk = (Button) root.findViewById(R.id.btnOk);
        mBtnOk.setOnClickListener(this);
        Profile profile = App.from(getActivity()).getProfile();
        ((ImageViewRemote) root.findViewById(R.id.ivAvatar)).setPhoto(profile.photo);
        ((TextView) root.findViewById(R.id.tvProfile)).setText(CacheProfile.getUserNameAgeString(profile));
        ((TextView) root.findViewById(R.id.tvWarningText)).setText(R.string.delete_account_warning);
    }

    @Override
    protected boolean isModalDialog() {
        return true;
    }

    @Override
    protected int getDialogLayoutRes() {
        return R.layout.dialog_delete_account;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnCancel:
                closeDialog();
                break;
            case R.id.btnOk:
                mBtnOk.setClickable(false);
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
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
                                            new AuthorizationManager().logout(getActivity());
                                        } else {
                                            fail(response.getResultCode(), response);
                                        }
                                    }

                                    @Override
                                    public void fail(int codeError, IApiResponse response) {
                                        Utils.showToastNotification(R.string.delete_account_error, Toast.LENGTH_SHORT);
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
                        });
                AlertDialog alertDialog = builder.create();
                alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        mBtnOk.setClickable(true);
                    }
                });
                alertDialog.show();
                break;
        }
    }

    private void closeDialog() {
        final Dialog dialog = getDialog();
        if (dialog != null) dialog.dismiss();
    }

    public static DeleteAccountDialog newInstance() {
        return new DeleteAccountDialog();
    }
}
