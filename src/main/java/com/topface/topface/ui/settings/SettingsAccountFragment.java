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

import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.LogoutRequest;
import com.topface.topface.requests.handlers.ApiHandler;
import com.topface.topface.ui.dialogs.DeleteAccountDialog;
import com.topface.topface.ui.fragments.BaseFragment;
import com.topface.topface.utils.Utils;
import com.topface.topface.utils.social.AuthToken;
import com.topface.topface.utils.social.AuthorizationManager;

public class SettingsAccountFragment extends BaseFragment implements OnClickListener {
    private View mLockerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_topface_account, container, false);
        mLockerView = root.findViewById(R.id.llvLogoutLoading);
        mLockerView.setVisibility(View.GONE);
        setNeedTitles(true);
        refreshActionBarTitles();
        Drawable icon = null;
        final AuthToken token = AuthToken.getInstance();

        if (token.getSocialNet().equals(AuthToken.SN_FACEBOOK)) {
            icon = getResources().getDrawable(R.drawable.fb_logo_account);
        } else if (token.getSocialNet().equals(AuthToken.SN_VKONTAKTE)) {
            icon = getResources().getDrawable(R.drawable.vk_logo_account);
        } else if (token.getSocialNet().equals(AuthToken.SN_TOPFACE)) {
            icon = getResources().getDrawable(R.drawable.ic_logo_account);
        } else if (token.getSocialNet().equals(AuthToken.SN_ODNOKLASSNIKI)) {
            icon = getResources().getDrawable(R.drawable.ico_ok_account);
        }
        TextView textName = (TextView) root.findViewById(R.id.tvText);
        textName.setText(App.getSessionConfig().getSocialAccountName());
        textName.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null);

        root.findViewById(R.id.btnLogout).setOnClickListener(this);
        root.findViewById(R.id.btnDeleteAccount).setOnClickListener(this);

        return root;
    }

    @Override
    protected String getTitle() {
        return getString(R.string.settings_account);
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
                        mLockerView.setVisibility(View.VISIBLE);
                        logoutRequest.callback(new ApiHandler() {
                            @Override
                            public void success(IApiResponse response) {
                                new AuthorizationManager().logout(getActivity());
                            }

                            @Override
                            public void fail(int codeError, IApiResponse response) {
                                FragmentActivity activity = getActivity();
                                if (activity != null) {
                                    mLockerView.setVisibility(View.GONE);
                                    Utils.showToastNotification(R.string.general_server_error, Toast.LENGTH_LONG);
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
        newFragment.show(getActivity().getSupportFragmentManager(), DeleteAccountDialog.TAG);
    }
}
