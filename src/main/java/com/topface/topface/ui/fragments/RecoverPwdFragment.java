package com.topface.topface.ui.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.topface.topface.R;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.RestorePwdRequest;
import com.topface.topface.requests.handlers.ApiHandler;
import com.topface.topface.ui.views.toolbar.utils.ToolbarManager;
import com.topface.topface.ui.views.toolbar.utils.ToolbarSettingsData;
import com.topface.topface.utils.Utils;

import java.util.Timer;
import java.util.TimerTask;

public class RecoverPwdFragment extends BaseFragment {

    public static final String ARG_EMAIL = "email";
    private Button mBtnRecover;
    private EditText mEdEmail;
    private ProgressBar mProgressBar;
    private TextView mRedAlertView;

    private Timer mTimer = new Timer();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View root = inflater.inflate(R.layout.fragment_recover_pwd, null);
        initViews(root);
        return root;
    }

    private void initViews(View root) {
        initEditViews(root);
        initButtonViews(root);
        initOtherViews(root);
    }

    private void initOtherViews(View root) {
        mProgressBar = (ProgressBar) root.findViewById(R.id.prsRecoverSending);
        mRedAlertView = (TextView) root.findViewById(R.id.tvRedAlert);
    }

    private void initButtonViews(View root) {
        mBtnRecover = (Button) root.findViewById(R.id.btnRecover);
        mBtnRecover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideButtons();
                removeRedAlert();
                Utils.hideSoftKeyboard(getActivity().getApplicationContext(), mEdEmail);
                RestorePwdRequest request = new RestorePwdRequest(getActivity());
                request.login = mEdEmail.getText().toString();
                request.callback(new ApiHandler() {
                    @Override
                    public void success(IApiResponse response) {
                        showButtons();
                        Utils.showToastNotification(R.string.recover_password_instructions, Toast.LENGTH_LONG);
                        getActivity().finish();
                    }

                    @Override
                    public void fail(int codeError, IApiResponse response) {
                        showButtons();
                        redAlert(R.string.enter_email_from_registration);
                    }
                }).exec();
            }
        });
        Bundle args = getArguments();
        if (args != null) {
            mBtnRecover.setEnabled(!TextUtils.isEmpty(getArguments().getString(ARG_EMAIL)));
        }
    }

    private void initEditViews(View root) {
        mEdEmail = (EditText) root.findViewById(R.id.edEmail);
        Bundle args = getArguments();
        if (args != null) {
            mEdEmail.setText(args.getString(ARG_EMAIL));
        }
        mEdEmail.setSelection(mEdEmail.getText().length());
        mEdEmail.addTextChangedListener(new TextWatcher() {
            String before = Utils.EMPTY;

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                before = s.toString();
            }

            @Override
            public void afterTextChanged(Editable s) {
                String after = s.toString();
                if (!before.equals(after)) {
                    mBtnRecover.setEnabled(Utils.isValidEmail(after));
                }
            }
        });
    }

    private void showButtons() {
        if (mBtnRecover != null && mProgressBar != null) {
            mBtnRecover.setVisibility(View.VISIBLE);
            mProgressBar.setVisibility(View.GONE);
        }
    }

    private void hideButtons() {
        if (mBtnRecover != null && mProgressBar != null) {
            mBtnRecover.setVisibility(View.INVISIBLE);
            mProgressBar.setVisibility(View.VISIBLE);
        }
    }

    private void redAlert(String text) {
        if (mRedAlertView != null) {
            if (text != null) {
                mRedAlertView.setText(text);
            }
            mRedAlertView.setAnimation(AnimationUtils.loadAnimation(getActivity().getApplicationContext(),
                    R.anim.slide_down_fade_in));
            mRedAlertView.setVisibility(View.VISIBLE);
            mTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (isAdded()) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                removeRedAlert();
                            }
                        });
                    }
                }
            }, RegistrationFragment.RED_ALERT_APPEARANCE_TIME);
        }
    }

    private void redAlert(int resId) {
        if(isAdded()) {
            redAlert(getString(resId));
        }
    }

    private void removeRedAlert() {
        if (mRedAlertView != null) {
            if (mRedAlertView.getVisibility() == View.VISIBLE) {
                mRedAlertView.setAnimation(AnimationUtils.loadAnimation(getActivity().getApplicationContext(),
                        android.R.anim.fade_out));
                mRedAlertView.setVisibility(View.INVISIBLE);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        ToolbarManager.INSTANCE.setToolbarSettings(new ToolbarSettingsData(getString(R.string.recovering_password)));
    }
}
