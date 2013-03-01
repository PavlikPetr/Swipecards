package com.topface.topface.ui.settings;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.topface.topface.R;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.ChangePasswordRequest;
import com.topface.topface.requests.handlers.ApiHandler;
import com.topface.topface.ui.fragments.BaseFragment;
import com.topface.topface.ui.views.LockerView;
import com.topface.topface.utils.ActionBar;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.social.AuthToken;

public class SettingsChangePasswordFragment extends BaseFragment implements OnClickListener{

    private LockerView mLockerView;
    private EditText mEdPassword;
    private EditText mEdPasswordConfirmation;
    private Button mBtnSave;
    private AuthToken mToken = AuthToken.getInstance();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_change_password, container, false);
        final FragmentActivity activity = getActivity();

        // Navigation bar
        ActionBar actionBar = getActionBar(root);
        actionBar.showBackButton(new OnClickListener() {

            @Override
            public void onClick(View v) {
                hideSoftKeyboard();
                activity.finish();
            }
        });
        actionBar.setTitleText(getString(R.string.password_changing));


        mLockerView = (LockerView) root.findViewById(R.id.llvLogoutLoading);
        mLockerView.setVisibility(View.GONE);

        mEdPassword = (EditText) root.findViewById(R.id.edPassword);
        mEdPasswordConfirmation = (EditText) root.findViewById(R.id.edPasswordConfirmation);

        mBtnSave = (Button) root.findViewById(R.id.btnSave);
        mBtnSave.setOnClickListener(this);

        return root;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnSave:
                hideSoftKeyboard();
                final String password = mEdPassword.getText().toString();
                final String passwordConfirmation = mEdPasswordConfirmation.getText().toString();
                if(password.trim().length() <= 0) {
                    Toast.makeText(getActivity(), R.string.enter_new_password, Toast.LENGTH_LONG).show();
                } else if (passwordConfirmation.trim().length() <= 0) {
                    Toast.makeText(getActivity(), R.string.enter_password_confirmation, Toast.LENGTH_LONG).show();
                } else if (!password.equals(passwordConfirmation)) {
                    Toast.makeText(getActivity(), R.string.passwords_mismatched, Toast.LENGTH_LONG).show();
                } else {
                    ChangePasswordRequest request = new ChangePasswordRequest(getActivity(),mToken.getPassword(),password);
                    lock();
                    request.callback(new ApiHandler() {
                        @Override
                        public void success(ApiResponse response) {
                            if (response.isCompleted()) {
                                Toast.makeText(getActivity(), R.string.passwords_changed, Toast.LENGTH_LONG).show();
                                mToken.saveToken(mToken.getUserId(), mToken.getLogin(), password);
                                CacheProfile.onPasswordChanged(getContext());
                                mEdPassword.getText().clear();
                                mEdPasswordConfirmation.getText().clear();
                            }
                        }

                        @Override
                        public void fail(int codeError, ApiResponse response) {
                            Toast.makeText(getActivity(), R.string.general_server_error, Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void always(ApiResponse response) {
                            super.always(response);
                            unlock();
                        }
                    }).exec();
                }
                break;
            default:
                break;
        }
    }

    private void hideSoftKeyboard() {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (mEdPassword != null) {
            imm.hideSoftInputFromWindow(mEdPassword.getWindowToken(), 0);
        }

        if (mEdPasswordConfirmation != null) {
            imm.hideSoftInputFromWindow(mEdPasswordConfirmation.getWindowToken(), 0);
        }
    }

    private void lock() {
        mLockerView.setVisibility(View.VISIBLE);
    }

    private void unlock() {
        mLockerView.setVisibility(View.GONE);
    }
}
