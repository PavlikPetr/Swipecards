package com.topface.topface.ui.settings;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.ChangeLoginRequest;
import com.topface.topface.requests.ConfirmRequest;
import com.topface.topface.requests.DataApiHandler;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.LogoutRequest;
import com.topface.topface.requests.ProfileRequest;
import com.topface.topface.requests.RemindRequest;
import com.topface.topface.requests.handlers.ApiHandler;
import com.topface.topface.ui.dialogs.DeleteAccountDialog;
import com.topface.topface.ui.fragments.BaseFragment;
import com.topface.topface.ui.views.LockerView;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.Utils;
import com.topface.topface.utils.social.AuthToken;
import com.topface.topface.utils.social.AuthorizationManager;

public class SettingsTopfaceAccountFragment extends BaseFragment implements OnClickListener {

    public static final String NEED_EXIT = "NEED_EXIT";
    private LockerView mLockerView;
    private EditText mEditText;
    private TextView mText;
    private Button mBtnChange;
    private Button mBtnLogout;
    private Button mBtnDelete;
    private final AuthToken mToken = AuthToken.getInstance();

    private static final int ACTION_RESEND_CONFIRM = 0;
    private static final int ACTION_CHANGE_EMAIL = 1;
    private static final int ACTION_CHANGE_PASSWORD = 2;
    private int mChangeButtonAction = ACTION_CHANGE_PASSWORD;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_topface_account, container, false);

        mLockerView = (LockerView) root.findViewById(R.id.llvLogoutLoading);
        mLockerView.setVisibility(View.GONE);

        String code = ((SettingsContainerActivity) getActivity()).getConfirmationCode();

        if (code != null) {
            ConfirmRequest request = new ConfirmRequest(getActivity(), AuthToken.getInstance().getLogin(), code);
            mLockerView.setVisibility(View.VISIBLE);
            request.callback(new ApiHandler() {
                @Override
                public void success(IApiResponse response) {
                    Toast.makeText(App.getContext(), R.string.email_confirmed, Toast.LENGTH_SHORT).show();
                    CacheProfile.emailConfirmed = true;
                    if (isAdded()) {
                        setViewsState();
                    }
                }

                @Override
                public void fail(int codeError, IApiResponse response) {
                    Toast.makeText(App.getContext(), R.string.general_server_error, Toast.LENGTH_SHORT).show();
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
            requestEmailConfirmedFlag();
        }

        initTextViews(root);
        initButtons(root);
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

    private void requestEmailConfirmedFlag() {
        ProfileRequest profileRequest = new ProfileRequest(getActivity());
        profileRequest.part = ProfileRequest.P_EMAIL_CONFIRMED;
        profileRequest.callback(new DataApiHandler<Boolean>() {
            @Override
            public void success(IApiResponse response) {
            }

            @Override
            protected void success(Boolean isEmailConfirmed, IApiResponse response) {
                CacheProfile.emailConfirmed = isEmailConfirmed;
                setViewsState();
            }

            @Override
            protected Boolean parseResponse(ApiResponse response) {
                return response.getJsonResult().optBoolean("emailConfirmed");
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

    private void initTextViews(ViewGroup root) {
        mEditText = (EditText) root.findViewById(R.id.edText);
        mEditText.setText(mToken.getLogin());
        mEditText.setSelection(Utils.getText(mEditText).length());
        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String text = s.toString();
                if (text.equals(mToken.getLogin())) {
                    setChangeBtnAction(ACTION_RESEND_CONFIRM);
                } else {
                    setChangeBtnAction(ACTION_CHANGE_EMAIL);
                }
            }
        });
        mText = (TextView) root.findViewById(R.id.tvText);
        mText.setText(mToken.getLogin());
    }

    private void setTextViewsState() {
        if (CacheProfile.emailConfirmed) {
            mEditText.setVisibility(View.GONE);
            mText.setVisibility(View.VISIBLE);
        } else {
            mEditText.setVisibility(View.VISIBLE);
            mText.setVisibility(View.GONE);
        }
    }

    private void initButtons(ViewGroup root) {
        mBtnChange = (Button) root.findViewById(R.id.btnChange);
        mBtnChange.setOnClickListener(this);
        mBtnLogout = (Button) root.findViewById(R.id.btnLogout);
        mBtnLogout.setOnClickListener(this);
        mBtnDelete = (Button) root.findViewById(R.id.btnDeleteAccount);
        mBtnDelete.setOnClickListener(this);
    }

    private void setButtonsState() {
        if (CacheProfile.emailConfirmed) {
            mBtnLogout.setVisibility(View.VISIBLE);
            setChangeBtnAction(ACTION_CHANGE_PASSWORD);
        } else {
            mBtnLogout.setVisibility(View.GONE);
            setChangeBtnAction(ACTION_RESEND_CONFIRM);
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

    @Override
    public void onClick(View v) {
        Utils.hideSoftKeyboard(getActivity(), mEditText);
        switch (v.getId()) {
            case R.id.btnLogout:
                if (CacheProfile.needToChangePassword(App.getContext())) {
                    Intent intent = new Intent(getActivity().getApplicationContext(), SettingsContainerActivity.class);
                    intent.putExtra(NEED_EXIT, true);
                    startActivityForResult(intent, SettingsContainerActivity.INTENT_CHANGE_PASSWORD);
                } else {
                    showExitPopup();
                }
                break;
            case R.id.btnChange:
                onChangeButtonClick();
                break;
            case R.id.btnDeleteAccount:
                deleteAccount();
                break;
            default:
                break;
        }
    }

    private void deleteAccount() {
        DeleteAccountDialog newFragment = DeleteAccountDialog.newInstance();
        try {
            newFragment.show(getActivity().getSupportFragmentManager(), DeleteAccountDialog.TAG);
        } catch (Exception e) {
            Debug.error(e);
        }
    }

    private void onChangeButtonClick() {
        switch (mChangeButtonAction) {
            case ACTION_RESEND_CONFIRM:
                RemindRequest remindRequest = new RemindRequest(getActivity());
                remindRequest.callback(new ApiHandler() {
                    @Override
                    public void success(IApiResponse response) {
                        Toast.makeText(App.getContext(), R.string.confirmation_successfully_sent, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void fail(int codeError, IApiResponse response) {
                        Toast.makeText(App.getContext(), R.string.general_server_error, Toast.LENGTH_SHORT).show();
                    }
                }).exec();
                break;
            case ACTION_CHANGE_EMAIL:
                final String email = Utils.getText(mEditText).trim();
                if (Utils.isValidEmail(email)) {
                    ChangeLoginRequest changeLoginRequest = new ChangeLoginRequest(getActivity(), email);
                    changeLoginRequest.callback(new ApiHandler() {
                        @Override
                        public void success(IApiResponse response) {
                            mToken.saveToken(mToken.getUserId(), email, mToken.getPassword());
                            setChangeBtnAction(ACTION_RESEND_CONFIRM);
                        }

                        @Override
                        public void fail(int codeError, IApiResponse response) {
                            Toast.makeText(App.getContext(), R.string.general_server_error, Toast.LENGTH_SHORT).show();
                        }
                    }).exec();
                } else {
                    Toast.makeText(App.getContext(), R.string.incorrect_email, Toast.LENGTH_SHORT).show();
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
                AuthorizationManager.logout(getActivity());
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

    private void showExitPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.settings_logout_msg);
        builder.setNegativeButton(R.string.general_no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.setPositiveButton(R.string.general_yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                logout();
            }
        });
        builder.create().show();
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
}
