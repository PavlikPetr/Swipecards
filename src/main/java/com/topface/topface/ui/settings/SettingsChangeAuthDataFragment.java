package com.topface.topface.ui.settings;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.topface.framework.utils.Debug;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.requests.ChangeLoginRequest;
import com.topface.topface.requests.ChangePasswordRequest;
import com.topface.topface.requests.ChangePwdFromAuthRequest;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.LogoutRequest;
import com.topface.topface.requests.handlers.ApiHandler;
import com.topface.topface.requests.handlers.ErrorCodes;
import com.topface.topface.ui.fragments.BaseFragment;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Utils;
import com.topface.topface.utils.social.AuthToken;
import com.topface.topface.utils.social.AuthorizationManager;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SettingsChangeAuthDataFragment extends BaseFragment {
    
private static final String NEED_EXIT = "restore_from_auth";
private static final String CHANGE_PASSWORD = "restore_from_auth";
private static final String RESTORE_FROM_AUTH = "restore_from_auth";
private static final String HASH = "hash";
private static final String PASSWORD = "password";
private static final String PASSWORD_CONFIRMATION = "password_confirmation";
private static final String OLD_PASSWORD = "old_password";
private static final String EMPTY = "";

    private View mLockerView;
    private EditText mEdMainField;
    private EditText mEdConfirmationField;
    private EditText mOldPassword;
    private Button mBtnSave;
    private AuthToken mToken = AuthToken.getInstance();
    private boolean mNeedExit;
    private boolean mChangePassword;
    private boolean mRestoreFromAuth;
    private String mHash;
    private String mPassword;
    private String mPasswordConfirmation;
    private String mOldPassword;

    @Bind(R.id.llvLogoutLoading)
    View mLockerView;
    @Bind(R.id.edMainField)
    EditText mEdMainField;
    @Bind(R.id.edConfirmationField)
    EditText mEdConfirmationField;
    @Bind(R.id.edOldPassword)
    EditText mOldPassword;
    @Bind(R.id.btnSave)
    Button mBtnSave;

    @SuppressWarnings("unused")
    @OnClick(R.id.btnSave)
    protected void saveBtnClick() {
        Utils.hideSoftKeyboard(getActivity(), mEdMainField, mEdConfirmationField);
        if (mChangePassword) {
            changePassword();
        } else {
            changeEmail();
        }
    }

    public static SettingsChangeAuthDataFragment newInstance(boolean needExit, boolean changePassword) {
        Bundle args = new Bundle();
        args.putBoolean(NEED_EXIT, needExit);
        args.putBoolean(CHANGE_PASSWORD, changePassword);
        SettingsChangeAuthDataFragment fragment = new SettingsChangeAuthDataFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static SettingsChangeAuthDataFragment newInstance(boolean changePassword, boolean restoreFromAuth, String hash) {
        SettingsChangeAuthDataFragment fragment = SettingsChangeAuthDataFragment.newInstance(false, changePassword);
        Bundle args = fragment.getArguments();
        args.putBoolean(RESTORE_FROM_AUTH, restoreFromAuth);
        args.putString(HASH, hash);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_change_auth_data, container, false);
        ButterKnife.bind(this, root);
        mChangePassword = getArguments().getBoolean(CHANGE_PASSWORD);
        mLockerView = root.findViewById(R.id.llvLogoutLoading);
        mLockerView.setVisibility(View.GONE);

        TextView mSetPasswordText = (TextView) root.findViewById(R.id.setPasswordText);

        if (mNeedExit && !mRestoreFromAuth) {
            mSetPasswordText.setVisibility(View.VISIBLE);
        }

        mEdMainField = (EditText) root.findViewById(R.id.edMainField);
        mEdConfirmationField = (EditText) root.findViewById(R.id.edConfirmationField);
        mEdOldPassword = (EditText) root.findViewById(R.id.edOldPassword);

        mBtnSave = (Button) root.findViewById(R.id.btnSave);
        ButterKnife.findById(root, R.id.setPasswordText).setVisibility(mNeedExit ? View.VISIBLE : View.GONE);
        if (mNeedExit) {
            mBtnSave.setText(getString(R.string.general_save_and_exit));
        }
        if (mChangePassword) {
            setTextOrHint(mEdMainField, mPassword, R.string.enter_new_password);
            mEdMainField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            if (mRestoreFromAuth) {
                mEdOldPassword.setVisibility(View.GONE);
            } else {
                setTextOrHint(mEdOldPassword, mOldPassword, R.string.enter_old_password);
                mEdOldPassword.setInputType(mEdMainField.getInputType());
            }
            setTextOrHint(mEdConfirmationField, mPasswordConfirmation, R.string.password_confirmation_hint);
            mEdConfirmationField.setInputType(mEdMainField.getInputType());
        } else {
            mEdMainField.setHint(R.string.email);
            mEdConfirmationField.setVisibility(View.GONE);
            mEdOldPassword.setVisibility(View.GONE);
        }
        return root;
    }

    private void setTextOrHint(EditText editText, String text, @StringRes int hintId) {
        if (TextUtils.isEmpty(text)) {
            editText.setHint(hintId);
        } else {
            editText.setText(text);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mBtnSave.setClickable(true);
    }

    @Override
    protected void restoreState(Bundle state) {
        if (state != null) {
            mNeedExit = state.getBoolean(NEED_EXIT);
            mChangePassword = state.getBoolean(CHANGE_PASSWORD);
            mRestoreFromAuth = state.getBoolean(RESTORE_FROM_AUTH);
            mHash = state.getString(HASH);
            mPassword = state.getString(PASSWORD, EMPTY);
            mPasswordConfirmation = state.getString(PASSWORD_CONFIRMATION, EMPTY);
            mOldPassword = state.getString(OLD_PASSWORD, EMPTY);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(NEED_EXIT, mNeedExit);
        outState.putBoolean(CHANGE_PASSWORD, mChangePassword);
        outState.putBoolean(RESTORE_FROM_AUTH, mRestoreFromAuth);
        outState.putString(HASH, mHash);
        outState.putString(PASSWORD, mEdMainField.getText().toString());
        outState.putString(PASSWORD_CONFIRMATION, mEdConfirmationField.getText().toString());
        outState.putString(OLD_PASSWORD, mEdOldPassword.getText().toString());
    }

    @Override
    protected String getTitle() {
        if (mChangePassword) {
            return getString(R.string.password_changing);
        } else {
            return getString(R.string.email_changing);
        }
    }

    private void changePasswordFromAuth() {
        final String password = mEdMainField.getText().toString();
        final String passwordConfirmation = mEdConfirmationField.getText().toString();
        if (isValidPassword(password, passwordConfirmation, null, false)) {
            ChangePwdFromAuthRequest request = new ChangePwdFromAuthRequest(getActivity(), mHash, password);
            request.callback(new ApiHandler() {
                @Override
                public void success(IApiResponse response) {
                    if (response.isCompleted()) {
                        Utils.showToastNotification(R.string.passwords_changed, Toast.LENGTH_LONG);
                        mEdMainField.getText().clear();
                        mEdConfirmationField.getText().clear();
                        mEdOldPassword.getText().clear();
                    }
                }

                @Override
                public void fail(int codeError, IApiResponse response) {
                    Utils.showToastNotification(R.string.general_server_error, Toast.LENGTH_SHORT);
                }
            }).exec();
        }
    }

    private boolean isValidPassword(String password, String passwordConfirmation, String oldPassword, boolean isNeedHandleOldPwd) {
        if (password.trim().length() <= 0) {
            Utils.showToastNotification(R.string.enter_new_password, Toast.LENGTH_LONG);
            return false;
        } else if (passwordConfirmation.trim().length() <= 0) {
            Utils.showToastNotification(R.string.enter_password_confirmation, Toast.LENGTH_LONG);
            return false;
        } else if (!password.equals(passwordConfirmation)) {
            Utils.showToastNotification(R.string.passwords_mismatched, Toast.LENGTH_LONG);
            return false;
        }
        if (!isNeedHandleOldPwd) {
            return true;
        } else {
            if (oldPassword.trim().length() <= 0) {
                Utils.showToastNotification(R.string.enter_old_password, Toast.LENGTH_LONG);
                return false;
            } else if (!oldPassword.equals(mToken.getPassword())) {
                Utils.showToastNotification(R.string.old_password_mismatched, Toast.LENGTH_LONG);
                return false;
            } else if (oldPassword.equals(password)) {
                Utils.showToastNotification(R.string.passwords_matched, Toast.LENGTH_LONG);
                return false;
            }
            return true;
        }
    }

    private void changePassword() {
        final String password = mEdMainField.getText().toString();
        final String passwordConfirmation = mEdConfirmationField.getText().toString();
        final String oldPassword = mEdOldPassword.getText().toString();
        if (isValidPassword(password, passwordConfirmation, oldPassword, true)) {
            ChangePasswordRequest request = new ChangePasswordRequest(getActivity(), mToken.getPassword(), password);
            lock();
            request.callback(new ApiHandler() {
                @Override
                public void success(IApiResponse response) {
                    if (response.isCompleted()) {
                        Utils.showToastNotification(R.string.passwords_changed, Toast.LENGTH_LONG);
                        mToken.saveToken(mToken.getUserSocialId(), mToken.getLogin(), password);
                        CacheProfile.onPasswordChanged(getContext());
                        mEdMainField.getText().clear();
                        mEdConfirmationField.getText().clear();
                        mEdOldPassword.getText().clear();
                        if (mNeedExit) {
                            logout();
                        }
                    }
                }

                @Override
                public void fail(int codeError, IApiResponse response) {
                    Utils.showToastNotification(R.string.general_server_error, Toast.LENGTH_SHORT);
                }

                @Override
                public void always(IApiResponse response) {
                    super.always(response);
                    if (!mNeedExit) {
                        unlock();
                    }
                }
            }).exec();
        }
    }


    private void changeEmail() {
        final String oldEmail = AuthToken.getInstance().getLogin();
        final String email = mEdMainField.getText().toString();
        if (Utils.isValidEmail(email)) {
            ChangeLoginRequest request = new ChangeLoginRequest(getActivity(), email);
            request.callback(new ApiHandler() {
                @Override
                public void success(IApiResponse response) {
                    if (response.isCompleted()) {
                        Utils.showToastNotification(R.string.confirmation_successfully_sent, Toast.LENGTH_LONG);
                        App.sendProfileAndOptionsRequests();
                        mToken.saveToken(mToken.getUserSocialId(), email, mToken.getPassword());
                        App.getConfig().rebuildUserConfig(oldEmail);
                        mEdMainField.getText().clear();
                    }
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
                }
            }).exec();
        } else {
            Toast.makeText(getActivity(), R.string.settings_invalid_email, Toast.LENGTH_LONG).show();
        }
    }

    private void logout() {
        LogoutRequest logoutRequest = new LogoutRequest(getActivity());
        lock();
        logoutRequest.callback(new ApiHandler() {
            @Override
            public void success(IApiResponse response) {
                new AuthorizationManager(getActivity()).logout(getActivity());
            }

            @Override
            public void fail(int codeError, IApiResponse response) {
                unlock();
            }


        }).exec();
    }

    private void lock() {
        if (mLockerView != null) {
            mLockerView.setVisibility(View.VISIBLE);
        }
    }

    private void unlock() {
        if (mLockerView != null) {
            mLockerView.setVisibility(View.GONE);
        }
    }

    private void showLogoutPopup(final String email) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(String.format(getActivity().getString(R.string.logout_if_email_already_registred), email));
        builder.setPositiveButton(R.string.general_exit, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                logout();
            }
        });
        builder.setNegativeButton(R.string.general_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(true);
        alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                mBtnSave.setClickable(true);
            }
        });
        alertDialog.show();
    }

}
