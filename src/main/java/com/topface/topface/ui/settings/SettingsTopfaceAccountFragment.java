package com.topface.topface.ui.settings;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.topface.framework.JsonUtils;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.data.BooleanEmailConfirmed;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.ChangeLoginRequest;
import com.topface.topface.requests.ConfirmRequest;
import com.topface.topface.requests.DataApiHandler;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.LogoutRequest;
import com.topface.topface.requests.ProfileRequest;
import com.topface.topface.requests.RemindRequest;
import com.topface.topface.requests.handlers.ApiHandler;
import com.topface.topface.requests.handlers.ErrorCodes;
import com.topface.topface.ui.BaseFragmentActivity;
import com.topface.topface.ui.IDialogListener;
import com.topface.topface.ui.analytics.TrackedDialogFragment;
import com.topface.topface.ui.dialogs.DeleteAccountDialog;
import com.topface.topface.ui.fragments.BaseFragment;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Utils;
import com.topface.topface.utils.config.UserConfig;
import com.topface.topface.utils.social.AuthToken;
import com.topface.topface.utils.social.AuthorizationManager;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;

public class SettingsTopfaceAccountFragment extends BaseFragment {

    public static final String NEED_EXIT = "NEED_EXIT";

    private final AuthToken mToken = AuthToken.getInstance();

    private static final int ACTION_RESEND_CONFIRM = 0;
    private static final int ACTION_CHANGE_EMAIL = 1;
    private static final int ACTION_CHANGE_PASSWORD = 2;
    private int mChangeButtonAction = ACTION_CHANGE_PASSWORD;
    private boolean mChangeEmail = false;

    @Bind(R.id.llvLogoutLoading)
    View mLockerView;
    @Bind(R.id.edText)
    EditText mEditText;
    @Bind(R.id.tvText)
    TextView mText;
    @Bind(R.id.btnChange)
    Button mBtnChange;
    @Bind(R.id.btnChangeEmail)
    Button mBtnChangeEmail;
    @Bind(R.id.btnLogout)
    Button mBtnLogout;
    @Bind(R.id.btnCodeWasSend)
    Button mBtnCodeWasSend;
    @Bind(R.id.txtCodeWasSend)
    TextView mTxtCodeWasSend;

    @SuppressWarnings("unused")
    @OnTextChanged(value = R.id.edText, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    protected void afterTextChanged(Editable s) {
        String text = s.toString();
        if (text.equals(mToken.getLogin())) {
            setChangeBtnAction(ACTION_RESEND_CONFIRM);
            mChangeEmail = false;
        } else {
            setChangeBtnAction(ACTION_CHANGE_EMAIL);
            mChangeEmail = true;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_topface_account, container, false);
        ButterKnife.bind(this, root);
        mLockerView.setVisibility(View.GONE);

        String code = ((SettingsContainerActivity) getActivity()).getConfirmationCode();

        if (code != null) {
            ConfirmRequest request = new ConfirmRequest(getActivity(), AuthToken.getInstance().getLogin(), code);
            mLockerView.setVisibility(View.VISIBLE);
            request.callback(new ApiHandler() {
                @Override
                public void success(IApiResponse response) {
                    Utils.showToastNotification(R.string.email_confirmed, Toast.LENGTH_SHORT);
                    App.from(getActivity()).getProfile().emailConfirmed = true;
                    if (isAdded()) {
                        setViewsState();
                    }
                }

                @Override
                public void fail(int codeError, IApiResponse response) {
                    Utils.showToastNotification(R.string.general_server_error, Toast.LENGTH_SHORT);
                }

                @Override
                public void always(IApiResponse response) {
                    super.always(response);
                    if (mLockerView != null) {
                        mLockerView.setVisibility(View.GONE);
                    }
                }
            }).exec();
        } else {
            requestEmailConfirmedFlag(false);
        }

        initTextViews();
        mBtnChange.setVisibility(View.VISIBLE);
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        setViewsState();
    }

    @Override
    protected String getTitle() {
        return getString(R.string.settings_account);
    }

    private void requestEmailConfirmedFlag(final boolean isShowEmailConfirmMessage) {
        ProfileRequest profileRequest = new ProfileRequest(getActivity());
        if (getActivity() instanceof BaseFragmentActivity) {
            ((BaseFragmentActivity) getActivity()).registerRequest(profileRequest);
        }
        profileRequest.callback(new DataApiHandler<Boolean>() {

            @Override
            protected void success(Boolean isEmailConfirmed, IApiResponse response) {
                App.from(getActivity()).getProfile().emailConfirmed = isEmailConfirmed;
                if (isShowEmailConfirmMessage) {
                    onProfileUpdated();
                }
                setViewsState();
            }

            @Override
            protected Boolean parseResponse(ApiResponse response) {
                return JsonUtils.fromJson(response.toString(), BooleanEmailConfirmed.class).isConfirmed;
            }

            @Override
            public void fail(int codeError, IApiResponse response) {
                Toast.makeText(App.getContext(), R.string.general_server_error, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void always(IApiResponse response) {
                super.always(response);
                unlock();
            }
        }).exec();
    }

    private void setViewsState() {
        setTextViewsState();
        setButtonsState();
    }

    private void initTextViews() {
        Drawable icon = getResources().getDrawable(R.drawable.ic_logo_account);
        mEditText.setText(mToken.getLogin());
        mEditText.setSelection(mEditText.getText().length());
        mText.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null);
        mText.setText(mToken.getLogin());
    }

    private void setTextViewsState() {
        if (App.from(getActivity()).getProfile().emailConfirmed) {
            mEditText.setVisibility(View.GONE);
            mText.setVisibility(View.VISIBLE);
            mBtnCodeWasSend.setVisibility(View.GONE);
            mTxtCodeWasSend.setVisibility(View.GONE);
        } else {
            mEditText.setVisibility(View.VISIBLE);
            mEditText.setText(mToken.getLogin());
            mEditText.setSelection(mEditText.getText().length());
            mText.setVisibility(View.GONE);
            mBtnCodeWasSend.setVisibility(View.VISIBLE);
            mTxtCodeWasSend.setVisibility(View.VISIBLE);
        }
    }

    private void setButtonsState() {
        if (App.from(getActivity()).getProfile().emailConfirmed) {
            mBtnLogout.setVisibility(View.VISIBLE);
            mBtnChangeEmail.setVisibility(View.VISIBLE);
            setChangeBtnAction(ACTION_CHANGE_PASSWORD);
        } else {
            mBtnLogout.setVisibility(View.GONE);
            mBtnChangeEmail.setVisibility(View.GONE);
            if (mChangeEmail) {
                setChangeBtnAction(ACTION_CHANGE_EMAIL);
            } else {
                setChangeBtnAction(ACTION_RESEND_CONFIRM);
            }
        }
    }

    private void setChangeBtnAction(int action) {
        switch (action) {
            case ACTION_CHANGE_PASSWORD:
                mBtnChange.setText(R.string.change_password);
                break;
            case ACTION_RESEND_CONFIRM:
                mBtnChange.setText(R.string.send_confirmation_email);
                break;
            case ACTION_CHANGE_EMAIL:
                mBtnChange.setText(R.string.change_email);
                break;
        }
        mChangeButtonAction = action;
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.btnLogout)
    protected void onLogoutButtonClick() {
        Utils.hideSoftKeyboard(getActivity(), mEditText);
        if (CacheProfile.needToChangePassword(App.getContext())) {
            Intent intent = new Intent(getActivity().getApplicationContext(), SettingsContainerActivity.class);
            intent.putExtra(NEED_EXIT, true);
            startActivityForResult(intent, SettingsContainerActivity.INTENT_CHANGE_PASSWORD);
        } else {
            showExitPopup();
        }
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.btnChangeEmail)
    protected void onChangeEmailButtonClick() {
        Utils.hideSoftKeyboard(getActivity(), mEditText);
        Intent intent = new Intent(getActivity().getApplicationContext(), SettingsContainerActivity.class);
        startActivityForResult(intent, SettingsContainerActivity.INTENT_CHANGE_EMAIL);
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.btnCodeWasSend)
    protected void updateProfile() {
        Utils.hideSoftKeyboard(getActivity(), mEditText);
        mText.setText(mToken.getLogin());
        requestEmailConfirmedFlag(true);
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.btnDeleteAccount)
    protected void deleteAccount() {
        Utils.hideSoftKeyboard(getActivity(), mEditText);
        DeleteAccountDialog newFragment = DeleteAccountDialog.newInstance();
        newFragment.show(getActivity().getSupportFragmentManager(), DeleteAccountDialog.TAG);
    }

    private void setEmailConfirmSent() {
        UserConfig config = App.getConfig().getUserConfig();
        config.saveButtonSendConfirmationPressed(true);
        config.saveConfig();
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.btnChange)
    protected void onChangeButtonClick() {
        Utils.hideSoftKeyboard(getActivity(), mEditText);
        switch (mChangeButtonAction) {
            case ACTION_RESEND_CONFIRM:
                RemindRequest remindRequest = new RemindRequest(getActivity());
                remindRequest.callback(new ApiHandler() {
                    @Override
                    public void success(IApiResponse response) {
                        setEmailConfirmSent();
                        Utils.showToastNotification(R.string.confirmation_successfully_sent, Toast.LENGTH_SHORT);
                    }

                    @Override
                    public void fail(int codeError, IApiResponse response) {
                        Utils.showToastNotification(R.string.general_server_error, Toast.LENGTH_SHORT);
                    }
                }).exec();
                break;
            case ACTION_CHANGE_EMAIL:
                final String email = Utils.getText(mEditText).trim();
                if (Utils.isValidEmail(email)) {
                    setClickableAccountManagmentButtons(false);
                    ChangeLoginRequest changeLoginRequest = new ChangeLoginRequest(getActivity(), email);
                    changeLoginRequest.callback(new ApiHandler() {
                        @Override
                        public void success(IApiResponse response) {
                            mToken.saveToken(mToken.getUserSocialId(), email, mToken.getPassword());
                            setChangeBtnAction(ACTION_RESEND_CONFIRM);
                            setEmailConfirmSent();
                        }

                        @Override
                        public void fail(int codeError, IApiResponse response) {
                            if (ErrorCodes.USER_ALREADY_REGISTERED == codeError) {
                                showLogoutPopup(email);
                            } else {
                                Utils.showToastNotification(R.string.general_server_error, Toast.LENGTH_SHORT);
                            }
                        }

                        @Override
                        public void always(IApiResponse response) {
                            super.always(response);
                            setClickableAccountManagmentButtons(true);
                        }
                    }).exec();
                } else {
                    Utils.showToastNotification(R.string.incorrect_email, Toast.LENGTH_SHORT);
                }
                break;
            case ACTION_CHANGE_PASSWORD:
                Intent intent = new Intent(getActivity().getApplicationContext(), SettingsContainerActivity.class);
                startActivityForResult(intent, SettingsContainerActivity.INTENT_CHANGE_PASSWORD);
                break;
        }
    }

    private void logout() {
        final LogoutRequest logoutRequest = new LogoutRequest(getActivity());
        mLockerView.setVisibility(View.VISIBLE);
        logoutRequest.callback(new ApiHandler() {
            @Override
            public void success(IApiResponse response) {
                new AuthorizationManager(getActivity()).logout(getActivity());
            }

            @Override
            public void fail(int codeError, IApiResponse response) {
                mLockerView.setVisibility(View.GONE);
                Activity activity = getActivity();
                if (activity != null) {
                    AuthorizationManager.showRetryLogoutDialog(activity, logoutRequest);
                }
            }
        }).exec();
    }

    private void showLogoutPopup(final String email) {
        final LogoutDialog logoutDialog = LogoutDialog.newInstance(email);
        logoutDialog.setDialogInterface(new IDialogListener() {
            @Override
            public void onPositiveButtonClick() {
                logout();
            }

            @Override
            public void onNegativeButtonClick() {
            }

            @Override
            public void onDismissListener() {
                setClickableAccountManagmentButtons(true);
            }
        });
        logoutDialog.show(getFragmentManager(), LogoutDialog.class.getName());
    }

    private void showExitPopup() {
        setClickableAccountManagmentButtons(false);
        final ExitDialog exitDialog = new ExitDialog();
        exitDialog.setDialogInterface(new IDialogListener() {
            @Override
            public void onPositiveButtonClick() {
                logout();
            }

            @Override
            public void onNegativeButtonClick() {
            }

            @Override
            public void onDismissListener() {
                setClickableAccountManagmentButtons(true);
            }
        });
        exitDialog.show(getFragmentManager(), ExitDialog.class.getName());
    }

    private void setClickableAccountManagmentButtons(boolean b) {
        if (mBtnLogout != null) {
            mBtnLogout.setClickable(b);
        }
        if (mBtnChange != null) {
            mBtnChange.setClickable(b);
        }
    }

    private void unlock() {
        if (mLockerView != null) {
            mLockerView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Utils.hideSoftKeyboard(getActivity(), mEditText);
    }

    private void onProfileUpdated() {
        if (App.from(getActivity()).getProfile().emailConfirmed) {
            Toast.makeText(App.getContext(), R.string.general_email_success_confirmed, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(App.getContext(), R.string.general_email_not_confirmed, Toast.LENGTH_SHORT).show();
        }
    }

    public static class ExitDialog extends TrackedDialogFragment {
        private IDialogListener mIDialogListener;

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return new AlertDialog.Builder(getActivity())
                    .setMessage(R.string.settings_logout_msg)
                    .setNegativeButton(R.string.general_no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            if (mIDialogListener != null) {
                                mIDialogListener.onNegativeButtonClick();
                            }
                        }
                    })
                    .setPositiveButton(R.string.general_yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (mIDialogListener != null) {
                                mIDialogListener.onPositiveButtonClick();
                            }
                        }
                    }).create();
        }

        public void setDialogInterface(IDialogListener dialogInterface) {
            mIDialogListener = dialogInterface;
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            super.onDismiss(dialog);
            if (mIDialogListener != null) {
                mIDialogListener.onDismissListener();
            }
        }
    }

    public static class LogoutDialog extends TrackedDialogFragment {
        private static final String EMAIL = "logout_dialog_email";
        private IDialogListener mIDialogListener;

        public static LogoutDialog newInstance(String email) {
            LogoutDialog logoutDialog = new LogoutDialog();
            Bundle bundle = new Bundle();
            bundle.putString(EMAIL, email);
            logoutDialog.setArguments(bundle);
            return logoutDialog;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            String email = "";
            Bundle bundle = getArguments();
            if (bundle != null) {
                email = bundle.getString(EMAIL);
            }
            return new AlertDialog.Builder(getActivity())
                    .setMessage(String.format(getActivity().getString(R.string.logout_if_email_already_registred), email))
                    .setPositiveButton(R.string.general_exit, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (mIDialogListener != null) {
                                mIDialogListener.onPositiveButtonClick();
                            }
                        }
                    })
                    .setNegativeButton(R.string.general_cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            if (mIDialogListener != null) {
                                mIDialogListener.onNegativeButtonClick();
                            }
                        }
                    }).create();
        }

        public void setDialogInterface(IDialogListener dialogInterface) {
            mIDialogListener = dialogInterface;
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            super.onDismiss(dialog);
            if (mIDialogListener != null) {
                mIDialogListener.onDismissListener();
            }
        }
    }
}
