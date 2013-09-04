package com.topface.topface.ui.settings;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import com.topface.topface.R;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.LogoutRequest;
import com.topface.topface.requests.handlers.ApiHandler;
import com.topface.topface.ui.dialogs.DeleteAccountDialog;
import com.topface.topface.ui.fragments.BaseFragment;
import com.topface.topface.ui.views.LockerView;
import com.topface.topface.utils.ActionBar;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.Settings;
import com.topface.topface.utils.social.AuthToken;
import com.topface.topface.utils.social.AuthorizationManager;

public class SettingsAccountFragment extends BaseFragment implements OnClickListener {

    private LockerView lockerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_account, container, false);

        // Navigation bar
        ActionBar actionBar = getActionBar(root);
        actionBar.showBackButton(new OnClickListener() {

            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });
        lockerView = (LockerView) root.findViewById(R.id.llvLogoutLoading);
        lockerView.setVisibility(View.GONE);
        actionBar.setTitleText(getString(R.string.settings_account));

        Drawable icon = null;
        final AuthToken token = AuthToken.getInstance();

        if (token.getSocialNet().equals(AuthToken.SN_FACEBOOK)) {
            icon = getResources().getDrawable(R.drawable.fb_logo_account);
        } else if (token.getSocialNet().equals(AuthToken.SN_VKONTAKTE)) {
            icon = getResources().getDrawable(R.drawable.vk_logo_account);
        } else if (token.getSocialNet().equals(AuthToken.SN_TOPFACE)) {
            icon = getResources().getDrawable(R.drawable.tf_logo_account);
        } else if (token.getSocialNet().equals(AuthToken.SN_ODNOKLASSNIKI)) {
            icon = getResources().getDrawable(R.drawable.ico_ok_account);
        }
        TextView textName = (TextView) root.findViewById(R.id.tvName);
        textName.setText(Settings.getInstance().getSocialAccountName());
        textName.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null);

        root.findViewById(R.id.btnLogout).setOnClickListener(this);
        root.findViewById(R.id.btnDeleteAccount).setOnClickListener(this);

        return root;
    }


    private void showExitPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.settings_logout_msg)
                .setNegativeButton(R.string.general_no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .setPositiveButton(R.string.general_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final LogoutRequest logoutRequest = new LogoutRequest(getActivity());
                        lockerView.setVisibility(View.VISIBLE);
                        logoutRequest.callback(new ApiHandler() {
                            @Override
                            public void success(IApiResponse response) {
                                AuthorizationManager.logout(getActivity());
                            }

                            @Override
                            public void fail(int codeError, IApiResponse response) {
                                FragmentActivity activity = getActivity();
                                if (activity != null) {
                                    lockerView.setVisibility(View.GONE);
                                    Toast.makeText(activity, R.string.general_server_error, Toast.LENGTH_LONG).show();
                                    AuthorizationManager.showRetryLogoutDialog(activity, logoutRequest);
                                }
                            }
                        }).exec();

                    }
                });
        builder.create().show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnDeleteAccount:
                deleteAccountDialog();
                break;
            case R.id.btnLogout:
                showExitPopup();
                break;
        }
    }

    private void deleteAccountDialog() {
        DeleteAccountDialog newFragment = DeleteAccountDialog.newInstance();
        try {
            newFragment.show(getActivity().getSupportFragmentManager(), DeleteAccountDialog.TAG);
        } catch (Exception e) {
            Debug.error(e);
        }
    }
}
