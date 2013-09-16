package com.topface.topface.ui.settings;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
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
import com.topface.topface.Ssid;
import com.topface.topface.Static;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.ChangePasswordRequest;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.LogoutRequest;
import com.topface.topface.requests.handlers.ApiHandler;
import com.topface.topface.ui.fragments.BaseFragment;
import com.topface.topface.ui.views.LockerView;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Utils;
import com.topface.topface.utils.social.AuthToken;
import com.topface.topface.utils.social.AuthorizationManager;

public class SettingsChangePasswordFragment extends BaseFragment implements OnClickListener {

    private LockerView mLockerView;
    private EditText mEdPassword;
    private EditText mEdPasswordConfirmation;
    private Button mBtnSave;
    private AuthToken mToken = AuthToken.getInstance();
    private boolean mNeedExit;

    public static SettingsChangePasswordFragment newInstance(boolean needExit) {
        Bundle args = new Bundle();
        args.putBoolean("needExit", needExit);
        SettingsChangePasswordFragment fragment = new SettingsChangePasswordFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater,container,savedInstanceState);
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_change_password, container, false);
        final FragmentActivity activity = getActivity();

        mLockerView = (LockerView) root.findViewById(R.id.llvLogoutLoading);
        mLockerView.setVisibility(View.GONE);

        TextView mSetPasswordText = (TextView) root.findViewById(R.id.setPasswordText);

        if (mNeedExit) {
            mSetPasswordText.setVisibility(View.VISIBLE);
        }

        mEdPassword = (EditText) root.findViewById(R.id.edPassword);
        mEdPasswordConfirmation = (EditText) root.findViewById(R.id.edPasswordConfirmation);

        mBtnSave = (Button) root.findViewById(R.id.btnSave);
        if (mNeedExit) {
            mBtnSave.setText(getString(R.string.general_save_and_exit));
        }
        mBtnSave.setOnClickListener(this);

        return root;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Utils.hideSoftKeyboard(getActivity(),mEdPassword,mEdPasswordConfirmation);
    }

    @Override
    protected void restoreState() {
        Bundle arguments = getArguments();
        if(arguments != null) {
            mNeedExit = arguments.getBoolean("needExit");
        }
    }

    @Override
    protected String getTitle() {
        return getString(R.string.password_changing);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnSave:
                Utils.hideSoftKeyboard(getActivity(),mEdPassword,mEdPasswordConfirmation);
                final String password = mEdPassword.getText().toString();
                final String passwordConfirmation = mEdPasswordConfirmation.getText().toString();
                if (password.trim().length() <= 0) {
                    Toast.makeText(App.getContext(), R.string.enter_new_password, Toast.LENGTH_LONG).show();
                } else if (passwordConfirmation.trim().length() <= 0) {
                    Toast.makeText(App.getContext(), R.string.enter_password_confirmation, Toast.LENGTH_LONG).show();
                } else if (!password.equals(passwordConfirmation)) {
                    Toast.makeText(App.getContext(), R.string.passwords_mismatched, Toast.LENGTH_LONG).show();
                } else {
                    ChangePasswordRequest request = new ChangePasswordRequest(getActivity(), mToken.getPassword(), password);
                    lock();
                    request.callback(new ApiHandler() {
                        @Override
                        public void success(IApiResponse response) {
                            if (response.isCompleted()) {
                                Toast.makeText(App.getContext(), R.string.passwords_changed, Toast.LENGTH_LONG).show();
                                mToken.saveToken(mToken.getUserId(), mToken.getLogin(), password);
                                CacheProfile.onPasswordChanged(getContext());
                                mEdPassword.getText().clear();
                                mEdPasswordConfirmation.getText().clear();
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
                break;
            default:
                break;
        }
    }

    private void logout() {
        LogoutRequest logoutRequest = new LogoutRequest(getActivity());
        lock();
        logoutRequest.callback(new ApiHandler() {
            @Override
            public void success(IApiResponse response) {
                AuthorizationManager.logout(getActivity());
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
}
