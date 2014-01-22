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
import com.topface.topface.requests.ChangeLoginRequest;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.RemindRequest;
import com.topface.topface.requests.handlers.ApiHandler;
import com.topface.topface.utils.Utils;
import com.topface.topface.utils.social.AuthToken;

public class ConfirmEmailDialog extends AbstractDialogFragment implements View.OnClickListener {

    public static final String TAG = "Topface_ConfirmEmailDialog_Tag";
    private EditText mEditEmailText;
    private Button mConfirmButton;
    private ProgressBar mProgressBar;

    @Override
    protected void initViews(View root) {
        getDialog().setCanceledOnTouchOutside(false);

        root.findViewById(R.id.btnClose).setOnClickListener(this);
        mConfirmButton = (Button) root.findViewById(R.id.btnSend);
        mConfirmButton.setOnClickListener(this);
        mProgressBar = (ProgressBar) root.findViewById(R.id.prsLoading);

        mEditEmailText = (EditText) root.findViewById(R.id.edEmail);
        AuthToken token = AuthToken.getInstance();
        if (token.getSocialNet().equals(AuthToken.SN_TOPFACE)) {
            mEditEmailText.setText(token.getLogin());
        }
    }

    @Override
    public int getDialogLayoutRes() {
        return R.layout.dialog_confirm_email;
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
            case R.id.btnClose:
                closeDialog();
                break;
            case R.id.btnSend:
                final String email = mEditEmailText.getText().toString().trim();
                if (Utils.isValidEmail(email)) {
                    if (AuthToken.getInstance().getLogin().equals(email)) {
                        resendEmailForConfirmation();
                    } else {
                        changeEmailAndSendConfirmation(email);
                    }
                } else {
                    Toast.makeText(App.getContext(), R.string.incorrect_email, Toast.LENGTH_SHORT).show();
                }
                break;

        }
    }

    private void changeEmailAndSendConfirmation(final String email) {
        ChangeLoginRequest changeLoginRequest = new ChangeLoginRequest(getActivity(), email);
        onRequestStart();
        changeLoginRequest.callback(new ApiHandler() {
            @Override
            public void success(IApiResponse response) {
                AuthToken token = AuthToken.getInstance();
                token.saveToken(token.getUserSocialId(), email, token.getPassword());
                Toast.makeText(App.getContext(), R.string.confirmation_successfully_sent, Toast.LENGTH_SHORT).show();
                closeDialog();
            }

            @Override
            public void fail(int codeError, IApiResponse response) {
                onRequestEnd();
                Toast.makeText(App.getContext(), R.string.general_server_error, Toast.LENGTH_SHORT).show();
            }
        }).exec();
    }

    private void resendEmailForConfirmation() {
        RemindRequest request = new RemindRequest(getActivity());
        onRequestStart();
        request.callback(new ApiHandler() {
            @Override
            public void success(IApiResponse response) {
                Toast.makeText(App.getContext(), R.string.confirmation_successfully_sent, Toast.LENGTH_SHORT).show();
                closeDialog();
            }

            @Override
            public void fail(int codeError, IApiResponse response) {
                onRequestEnd();
                Toast.makeText(App.getContext(), R.string.general_server_error, Toast.LENGTH_SHORT).show();
            }
        }).exec();
    }

    private void closeDialog() {
        final Dialog dialog = getDialog();
        if (dialog != null) dialog.dismiss();
    }

    public static ConfirmEmailDialog newInstance() {
        ConfirmEmailDialog dialog = new ConfirmEmailDialog();
        dialog.setStyle(DialogFragment.STYLE_NO_TITLE, R.style.Theme_Topface);
        return dialog;
    }

    private void onRequestStart() {
        mConfirmButton.setVisibility(View.INVISIBLE);
        mProgressBar.setVisibility(View.VISIBLE);
    }

    private void onRequestEnd() {
        mConfirmButton.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.GONE);
    }
}
