package com.topface.topface.ui.dialogs;

import android.app.Dialog;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.ChangeLoginRequest;
import com.topface.topface.requests.DataApiHandler;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.ProfileRequest;
import com.topface.topface.requests.RemindRequest;
import com.topface.topface.requests.handlers.ApiHandler;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Utils;
import com.topface.topface.utils.config.UserConfig;
import com.topface.topface.utils.social.AuthToken;

public class ConfirmEmailDialog extends AbstractModalDialog implements View.OnClickListener {

    public static final String TAG = "Topface_ConfirmEmailDialog_Tag";
    private EditText mEditEmailText;
    private Button mConfirmButton;
    private Button mConfirmed;
    private ProgressBar mProgressBar;

    @Override
    protected void initContentViews(View root) {
        getDialog().setCanceledOnTouchOutside(false);
        mConfirmButton = (Button) root.findViewById(R.id.btnSend);
        mConfirmed = (Button) root.findViewById(R.id.btnConfirmed);
        mConfirmed.setOnClickListener(this);
        mConfirmButton.setOnClickListener(this);
        setButtonConfirmedVisibility();
        mProgressBar = (ProgressBar) root.findViewById(R.id.prsLoading);
        mEditEmailText = (EditText) root.findViewById(R.id.edEmail);
        AuthToken token = AuthToken.getInstance();
        if (token.getSocialNet().equals(AuthToken.SN_TOPFACE)) {
            mEditEmailText.setText(token.getLogin());
        }
    }

    @Override
    protected int getContentLayoutResId() {
        return R.layout.dialog_confirm_email;
    }

    @Override
    public void onPause() {
        super.onPause();
        Utils.hideSoftKeyboard(getActivity(), mEditEmailText);
    }


    @Override
    protected void onCloseButtonClick(View v) {
        closeDialog();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!AuthToken.getInstance().getSocialNet().equals(AuthToken.SN_TOPFACE)) {
            closeDialog();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnSend:
                UserConfig config = App.getConfig().getUserConfig();
                config.setSendConfirmationEmailState(true);
                setButtonConfirmedVisibility(true);
                config.saveConfig();
                final String email = mEditEmailText.getText().toString().trim();
                if (Utils.isValidEmail(email)) {
                    if (AuthToken.getInstance().getLogin().equals(email)) {
                        resendEmailForConfirmation();
                    } else {
                        changeEmailAndSendConfirmation(email);
                    }
                } else {
                    showToastMessage(R.string.incorrect_email);
                }
                break;
            case R.id.btnConfirmed:
                requestEmailConfirmed();
                break;
        }
    }

    private void setButtonConfirmedVisibility() {
        setButtonConfirmedVisibility(App.getConfig().getUserConfig().isButtonSendConfirmationClicked());
    }

    private void setButtonConfirmedVisibility(boolean isConfirmationSend) {
        mConfirmed.setVisibility(isConfirmationSend ? View.VISIBLE : View.GONE);
    }

    private void changeEmailAndSendConfirmation(final String email) {
        ChangeLoginRequest changeLoginRequest = new ChangeLoginRequest(getActivity(), email);
        onRequestStart();
        changeLoginRequest.callback(new ApiHandler() {
            @Override
            public void success(IApiResponse response) {
                AuthToken token = AuthToken.getInstance();
                token.saveToken(token.getUserSocialId(), email, token.getPassword());
                showToastMessage(R.string.confirmation_successfully_sent);
            }

            @Override
            public void fail(int codeError, IApiResponse response) {
                showToastMessage(R.string.general_server_error);
            }

            @Override
            public void always(IApiResponse response) {
                onRequestEnd();
            }
        }).exec();
    }

    private void resendEmailForConfirmation() {
        RemindRequest request = new RemindRequest(getActivity());
        onRequestStart();
        request.callback(new ApiHandler() {
            @Override
            public void success(IApiResponse response) {
                showToastMessage(R.string.confirmation_successfully_sent);
            }

            @Override
            public void fail(int codeError, IApiResponse response) {
                showToastMessage(R.string.general_server_error);
            }

            @Override
            public void always(IApiResponse response) {
                onRequestEnd();
            }
        }).exec();
    }

    private void closeDialog() {
        Utils.hideSoftKeyboard(getActivity(), mEditEmailText);
        final Dialog dialog = getDialog();
        if (dialog != null) dismiss();
    }

    public static ConfirmEmailDialog newInstance() {
        ConfirmEmailDialog dialog = new ConfirmEmailDialog();
        dialog.setStyle(DialogFragment.STYLE_NO_TITLE, R.style.Theme_Topface);
        return dialog;
    }

    private void onRequestStart() {
        mConfirmButton.setVisibility(View.INVISIBLE);
        mProgressBar.setVisibility(View.VISIBLE);
        setButtonConfirmedVisibility(false);
    }

    private void onRequestEnd() {
        mConfirmButton.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.GONE);
        setButtonConfirmedVisibility();
    }

    private void requestEmailConfirmed() {
        ProfileRequest profileRequest = new ProfileRequest(getActivity());
        onRequestStart();
        profileRequest.part = ProfileRequest.P_EMAIL_CONFIRMED;
        profileRequest.callback(new DataApiHandler<Boolean>() {
            @Override
            protected void success(Boolean isEmailConfirmed, IApiResponse response) {
                CacheProfile.emailConfirmed = isEmailConfirmed;
                onProfileUpdated();
            }

            @Override
            protected Boolean parseResponse(ApiResponse response) {
                return response.getJsonResult().optBoolean("emailConfirmed");
            }

            @Override
            public void fail(int codeError, IApiResponse response) {
                showToastMessage(R.string.general_server_error);
            }

            @Override
            public void always(IApiResponse response) {
                super.always(response);
                onRequestEnd();
            }
        }).exec();
    }

    private void onProfileUpdated() {
        if (CacheProfile.emailConfirmed) {
            showToastMessage(R.string.general_email_success_confirmed);
            closeDialog();
        } else {
            showToastMessage(R.string.general_email_not_confirmed);
        }
    }

    private void showToastMessage(int textId) {
        Toast.makeText(App.getContext(), textId, Toast.LENGTH_SHORT).show();
    }
}
