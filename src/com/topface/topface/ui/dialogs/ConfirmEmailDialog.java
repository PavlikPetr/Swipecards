package com.topface.topface.ui.dialogs;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.ChangeLoginRequest;
import com.topface.topface.requests.RemindRequest;
import com.topface.topface.requests.handlers.ApiHandler;
import com.topface.topface.utils.Utils;
import com.topface.topface.utils.social.AuthToken;

public class ConfirmEmailDialog extends DialogFragment implements View.OnClickListener {

    public static final String TAG = "Topface_ConfirmEmailDialog_Tag";
    private EditText mEditEmailText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.dialog_confirm_email, container, false);

        setTransparentBackground();
        getDialog().setCanceledOnTouchOutside(false);

        root.findViewById(R.id.btnClose).setOnClickListener(this);
        root.findViewById(R.id.btnSend).setOnClickListener(this);

        mEditEmailText = (EditText) root.findViewById(R.id.edEmail);
        AuthToken token = AuthToken.getInstance();
        if (token.getSocialNet().equals(AuthToken.SN_TOPFACE)) {
            mEditEmailText.setText(token.getLogin());
        }

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!AuthToken.getInstance().getSocialNet().equals(AuthToken.SN_TOPFACE)) {
            closeDialog();
        }
    }

    private void setTransparentBackground() {
        ColorDrawable color = new ColorDrawable(Color.BLACK);
        color.setAlpha(175);
        getDialog().getWindow().setBackgroundDrawable(color);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
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

        changeLoginRequest.callback(new ApiHandler() {
            @Override
            public void success(ApiResponse response) {
                AuthToken token = AuthToken.getInstance();
                token.saveToken(token.getUserId(), email, token.getPassword());
                Toast.makeText(App.getContext(), R.string.confirmation_successfully_sent, Toast.LENGTH_SHORT).show();
                closeDialog();
            }

            @Override
            public void fail(int codeError, ApiResponse response) {
                Toast.makeText(App.getContext(), R.string.general_server_error, Toast.LENGTH_SHORT).show();
            }
        }).exec();
    }

    private void resendEmailForConfirmation() {
        RemindRequest request = new RemindRequest(getActivity());
        request.callback(new ApiHandler() {
            @Override
            public void success(ApiResponse response) {
                Toast.makeText(App.getContext(), R.string.confirmation_successfully_sent, Toast.LENGTH_SHORT).show();
                closeDialog();
            }

            @Override
            public void fail(int codeError, ApiResponse response) {
                Toast.makeText(App.getContext(), R.string.general_server_error, Toast.LENGTH_SHORT).show();
            }
        });
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
}
