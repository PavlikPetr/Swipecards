package com.topface.topface.ui.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.text.method.TransformationMethod;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.requests.ApiRequest;
import com.topface.topface.requests.handlers.ErrorCodes;
import com.topface.topface.ui.PasswordRecoverActivity;
import com.topface.topface.ui.RegistrationActivity;
import com.topface.topface.utils.EasyTracker;
import com.topface.topface.utils.Utils;
import com.topface.topface.utils.config.SessionConfig;
import com.topface.topface.utils.social.AuthToken;
import com.topface.topface.utils.social.STAuthMails;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Own Topface authorization
 */
public class TopfaceAuthFragment extends BaseAuthFragment {

    private View mLogo;
    private Button mTFButton;
    private AutoCompleteTextView mLogin;
    private EditText mPassword;
    private ImageButton mShowPassword;
    private TextView mBackButton;
    private RelativeLayout mWrongPasswordAlertView;
    private TextView mWrongDataTextView;
    private TextView mCreateAccountButton;
    private Timer mTimer = new Timer();
    private ProgressBar mLoginSendingProgress;
    private TextView mRecoverPwd;
    private String mEmailForRestorePassword;
    private String mEmailForNewReg;

    private View.OnClickListener mCredentialsEditListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            removeRedAlert();
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View root = inflater.inflate(R.layout.layout_topface_signin, null);
        initViews(root);

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        removeRedAlert();
        mPassword.setText("");
    }

    @Override
    public void onPause() {
        super.onPause();
        Utils.hideSoftKeyboard(getActivity(), mLogin, mPassword);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mTimer.cancel();
    }

    @Override
    protected void initViews(View root) {
        super.initViews(root);

        mLogo = root.findViewById(R.id.ivTopfaceLogo);
        mLogin = (AutoCompleteTextView) root.findViewById(R.id.edLogin);
        //fill autocomplete data (emails)
        STAuthMails.initInputField(getActivity(), mLogin);
        mTFButton = (Button) root.findViewById(R.id.btnLogin);
        mTFButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onTFLoginClick();
            }
        });
        mBackButton = (TextView) root.findViewById(R.id.tvBackToMainAuth);
        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeRedAlert();
                Utils.hideSoftKeyboard(getActivity(), mLogin, mPassword);
                getActivity().onBackPressed();
            }
        });
        mWrongPasswordAlertView = (RelativeLayout) root.findViewById(R.id.redAlert);
        mWrongDataTextView = (TextView) root.findViewById(R.id.redAlertTextView);
        mCreateAccountButton = (TextView) root.findViewById(R.id.redAlertButton);
        mCreateAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EasyTracker.sendEvent("Registration", "StartActivity", "FromAuth", 1L);
                Intent intent = new Intent(getActivity(), RegistrationActivity.class);
                intent.putExtra(RecoverPwdFragment.ARG_EMAIL, mEmailForNewReg);
                startActivityForResult(intent, RegistrationActivity.INTENT_REGISTRATION);
            }
        });
        mPassword = (EditText) root.findViewById(R.id.edPassword);
        mPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    handled = true;
                    onTFLoginClick();
                }
                return handled;
            }
        });
        mShowPassword = (ImageButton) root.findViewById(R.id.ivShowPassword);
        mShowPassword.setOnClickListener(new View.OnClickListener() {
            boolean toggle = false;
            TransformationMethod passwordMethod = new PasswordTransformationMethod();

            @Override
            public void onClick(View v) {
                toggle = !toggle;
                mShowPassword.setImageResource(toggle ? R.drawable.ic_show_password : R.drawable.ic_show_password_pressed);
                mPassword.setTransformationMethod(toggle ? null : passwordMethod);
                Editable text = mPassword.getText();
                if (text != null) {
                    mPassword.setSelection(text.length());
                }
            }
        });
        mLoginSendingProgress = (ProgressBar) root.findViewById(R.id.prsLoginSending);
        mRecoverPwd = (TextView) root.findViewById(R.id.tvRecoverPwd);
        mRecoverPwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), PasswordRecoverActivity.class);
                intent.putExtra(RecoverPwdFragment.ARG_EMAIL, mEmailForRestorePassword);
                startActivityForResult(intent, PasswordRecoverActivity.INTENT_RECOVER_PASSWORD);
            }
        });
        mRecoverPwd.setVisibility(View.GONE);

        mLogin.setOnClickListener(mCredentialsEditListener);
        mPassword.setOnClickListener(mCredentialsEditListener);
    }

    @Override
    protected int getRootId() {
        return R.id.tf_auth_root;
    }

    @Override
    protected void processAuthError(int codeError, ApiRequest request) {
        super.processAuthError(codeError, request);

        switch (codeError) {
            case ErrorCodes.INCORRECT_LOGIN:
            case ErrorCodes.UNKNOWN_SOCIAL_USER:
                redAlert(R.string.incorrect_login);
                //сохранить email введенный при авторизации
                mEmailForNewReg = Utils.getText(mLogin).trim();
                break;
            case ErrorCodes.INCORRECT_PASSWORD:
                redAlert(R.string.incorrect_password);
                mRecoverPwd.setVisibility(View.VISIBLE);
                //сохранить корректный логин на случай изменения
                mEmailForRestorePassword = Utils.getText(mLogin).trim();
                break;
            case ErrorCodes.MISSING_REQUIRE_PARAMETER:
                redAlert(R.string.empty_fields);
                break;
        }
        if (codeError != ErrorCodes.USER_DELETED) {
            AuthToken.getInstance().removeToken();
        }
    }

    @Override
    protected void showRetrier() {
        super.showRetrier();
        mLogo.setVisibility(View.GONE);
        mTFButton.setVisibility(View.GONE);
        mLogin.setVisibility(View.GONE);
        mPassword.setVisibility(View.GONE);
        mShowPassword.setVisibility(View.GONE);
        mLoginSendingProgress.setVisibility(View.GONE);
        mRecoverPwd.setVisibility(View.GONE);
    }

    @Override
    protected void hideRetrier() {
        super.hideRetrier();
        mLogo.setVisibility(View.VISIBLE);
        mTFButton.setVisibility(View.VISIBLE);
        mLogin.setVisibility(View.VISIBLE);
        mPassword.setVisibility(View.VISIBLE);
        mShowPassword.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onOptionsAndProfileSuccess() {
        if (isAdded()) {
            getActivity().finish();
        }
    }

    private void btnTFClick() {
        String emailLogin = Utils.getText(mLogin).trim();
        String password = Utils.getText(mPassword);
        if (TextUtils.isEmpty(emailLogin) || TextUtils.isEmpty(password.trim())) {
            redAlert(R.string.empty_fields);
            showButtons();
            return;
        } else if (!Utils.isValidEmail(emailLogin)) {
            redAlert(R.string.incorrect_login);
            showButtons();
            return;
        }
        AuthToken token = AuthToken.getInstance();
        token.saveToken(emailLogin, emailLogin, password);
        SessionConfig sessionConfig = App.getSessionConfig();
        sessionConfig.setSocialAccountEmail(emailLogin);
        sessionConfig.saveConfig();

        removeRedAlert();
        mRecoverPwd.setVisibility(View.GONE);

        auth(token);
    }

    private void onTFLoginClick() {
        btnTFClick();
        Utils.hideSoftKeyboard(getActivity(), mLogin, mPassword);
    }

    private void redAlert(int resId) {
        redAlert(getString(resId));
    }

    private void redAlert(String text) {
        if (mWrongPasswordAlertView != null) {
            if (text != null) {
                mWrongDataTextView.setText(text);
            }
            mWrongPasswordAlertView.setAnimation(AnimationUtils.loadAnimation(getActivity(),
                    R.anim.slide_down_fade_in));
            mWrongPasswordAlertView.setVisibility(View.VISIBLE);
            mWrongDataTextView.setVisibility(View.VISIBLE);
            if (text != null && text.equals(getString(R.string.incorrect_login))) {
                mCreateAccountButton.setVisibility(View.VISIBLE);
            } else {
                mCreateAccountButton.setVisibility(View.GONE);
                mTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                removeRedAlert();
                            }
                        });
                    }
                }, Static.RED_ALERT_APPEARANCE_TIME);
            }
        }
    }

    private void removeRedAlert() {
        if (mWrongPasswordAlertView != null && mWrongPasswordAlertView.getVisibility() == View.VISIBLE) {
            mWrongPasswordAlertView.setAnimation(AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_out));
            mWrongPasswordAlertView.setVisibility(View.GONE);
            mWrongDataTextView.setVisibility(View.GONE);
        }
    }

    @Override
    protected void showButtons() {
        mBackButton.setVisibility(View.VISIBLE);
        mLogo.setVisibility(View.VISIBLE);
        mTFButton.setVisibility(View.VISIBLE);
        mLogin.setEnabled(true);
        mPassword.setEnabled(true);
        mShowPassword.setEnabled(true);
        mRecoverPwd.setEnabled(true);
    }

    @Override
    protected void hideButtons() {
        mBackButton.setVisibility(View.GONE);
        mTFButton.setVisibility(View.INVISIBLE);
        mLogin.setEnabled(false);
        mPassword.setEnabled(false);
        mShowPassword.setEnabled(false);
        mRecoverPwd.setEnabled(false);
    }

    @Override
    protected void showProgress() {
        hideButtons();
        mLoginSendingProgress.setVisibility(View.VISIBLE);
    }

    @Override
    protected void hideProgress() {
        mLoginSendingProgress.setVisibility(View.GONE);
    }

    @Override
    protected void onSuccessAuthorization(AuthToken token) {
        STAuthMails.addEmail(token.getLogin());
        Activity activity = getActivity();
        if (activity != null) {
            activity.setResult(Activity.RESULT_OK);
            activity.finish();
            activity.overridePendingTransition(0, 0);
        }
    }

    @Override
    protected void authorizationFailed(int codeError, ApiRequest request) {
        mEmailForRestorePassword = null;
        super.authorizationFailed(codeError, request);
    }
}
