package com.topface.topface.ui.settings;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.requests.ChangeLoginRequest;
import com.topface.topface.requests.ChangePasswordRequest;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.LogoutRequest;
import com.topface.topface.requests.handlers.ApiHandler;
import com.topface.topface.requests.handlers.ErrorCodes;
import com.topface.topface.ui.fragments.BaseFragment;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Utils;
import com.topface.topface.utils.social.AuthToken;
import com.topface.topface.utils.social.AuthorizationManager;

public class SettingsChangeAuthData extends BaseFragment implements OnClickListener {

    private View mLockerView;
    private EditText mEdMainField;
    private EditText mEdConfirmationField;
    private EditText mOldPassword;
    private Button mBtnSave;
    private AuthToken mToken = AuthToken.getInstance();
    private boolean mNeedExit;
    private boolean mChangePassword;

    public static SettingsChangeAuthData newInstance(boolean needExit, boolean changePassword) {
        Bundle args = new Bundle();
        args.putBoolean("needExit", needExit);
        args.putBoolean("changePassword", changePassword);
        SettingsChangeAuthData fragment = new SettingsChangeAuthData();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_change_auth_data, container, false);
        mChangePassword = getArguments().getBoolean("changePassword");
        mLockerView = root.findViewById(R.id.llvLogoutLoading);
        mLockerView.setVisibility(View.GONE);

        TextView mSetPasswordText = (TextView) root.findViewById(R.id.setPasswordText);

        if (mNeedExit) {
            mSetPasswordText.setVisibility(View.VISIBLE);
        }

        mEdMainField = (EditText) root.findViewById(R.id.edMainField);
        mEdConfirmationField = (EditText) root.findViewById(R.id.edConfirmationField);
        mOldPassword = (EditText) root.findViewById(R.id.edOldPassword);

        mBtnSave = (Button) root.findViewById(R.id.btnSave);
        if (mNeedExit) {
            mBtnSave.setText(getString(R.string.general_save_and_exit));
        }
        mBtnSave.setOnClickListener(this);
        if (mChangePassword) {
            mEdMainField.setHint(R.string.password);
            mEdConfirmationField.setHint(R.string.password_confirmation_hint);
            mEdMainField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            mEdConfirmationField.setHint(R.string.password_confirmation_hint);
            mEdConfirmationField.setInputType(mEdMainField.getInputType());
            mOldPassword.setHint(R.string.enter_old_password);
            mOldPassword.setInputType(mEdMainField.getInputType());
        } else {
            mEdMainField.setHint(R.string.email);
            mEdConfirmationField.setVisibility(View.GONE);
        }
        return root;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Utils.hideSoftKeyboard(getActivity(), mEdMainField, mEdConfirmationField);
    }

    @Override
    public void onResume() {
        super.onResume();
        mBtnSave.setClickable(true);
    }

    @Override
    protected void restoreState() {
        Bundle arguments = getArguments();
        if (arguments != null) {
            mNeedExit = arguments.getBoolean("needExit");
            mChangePassword = getArguments().getBoolean("changePassword");
        }
    }

    @Override
    protected String getTitle() {
        if (mChangePassword) {
            return getString(R.string.password_changing);
        } else {
            return getString(R.string.email_changing);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnSave:
                Utils.hideSoftKeyboard(getActivity(), mEdMainField, mEdConfirmationField);
                if (mChangePassword) {
                    changePassword();
                } else {
                    changeEmail();
                }
                break;
            default:
                break;
        }
    }

    private void changePassword() {
        final String password = mEdMainField.getText().toString();
        final String passwordConfirmation = mEdConfirmationField.getText().toString();
        final String oldPassword = mOldPassword.getText().toString();
        if (password.trim().length() <= 0) {
            Toast.makeText(App.getContext(), R.string.enter_new_password, Toast.LENGTH_LONG).show();
        } else if (oldPassword.trim().length() <= 0) {
            Toast.makeText(App.getContext(), R.string.enter_old_password, Toast.LENGTH_LONG).show();
        } else if (passwordConfirmation.trim().length() <= 0) {
            Toast.makeText(App.getContext(), R.string.enter_password_confirmation, Toast.LENGTH_LONG).show();
        } else if (!password.equals(passwordConfirmation)) {
            Toast.makeText(App.getContext(), R.string.passwords_mismatched, Toast.LENGTH_LONG).show();
        } else if (!oldPassword.equals(mToken.getPassword())) {
            Toast.makeText(App.getContext(), R.string.old_password_mismatched, Toast.LENGTH_LONG).show();
        } else {
            ChangePasswordRequest request = new ChangePasswordRequest(getActivity(), oldPassword, password);
            lock();
            request.callback(new ApiHandler() {
                @Override
                public void success(IApiResponse response) {
                    if (response.isCompleted()) {
                        Toast.makeText(App.getContext(), R.string.passwords_changed, Toast.LENGTH_LONG).show();
                        mToken.saveToken(mToken.getUserSocialId(), mToken.getLogin(), password);
                        CacheProfile.onPasswordChanged(getContext());
                        mEdMainField.getText().clear();
                        mEdConfirmationField.getText().clear();
                        mOldPassword.getText().clear();
                        mBtnSave.setClickable(false);
                        if (mNeedExit) {
                            logout();
                        }
                    }
                }

                @Override
                public void fail(int codeError, IApiResponse response) {
                    Toast.makeText(App.getContext(), R.string.general_server_error, Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(App.getContext(), R.string.confirmation_successfully_sent, Toast.LENGTH_LONG).show();
                        App.sendProfileAndOptionsRequests();
                        mToken.saveToken(mToken.getUserSocialId(), email, mToken.getPassword());
                        App.getConfig().rebuildUserConfig(oldEmail);
                        mEdMainField.getText().clear();
                        mBtnSave.setClickable(false);
                    }
                }

                @Override
                public void fail(int codeError, IApiResponse response) {
                    if (ErrorCodes.USER_ALREADY_REGISTERED == codeError) {
                        showLogoutPopup(email);
                    } else {
                        Toast.makeText(App.getContext(), R.string.general_server_error, Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void always(IApiResponse response) {
                    super.always(response);
                    mBtnSave.setClickable(true);
                }
            }).exec();
        } else {
            Toast.makeText(getActivity(), R.string.settings_invalid_email, Toast.LENGTH_LONG).show();
            mBtnSave.setClickable(true);
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
