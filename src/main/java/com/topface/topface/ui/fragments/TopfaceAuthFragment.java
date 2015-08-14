package com.topface.topface.ui.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.text.method.TransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnEditorAction;

/**
 * Own Topface authorization
 */
public class TopfaceAuthFragment extends BaseAuthFragment {

    @Bind(R.id.btnEntrance)
    Button mTFButton;
    @Bind(R.id.etMail)
    AutoCompleteTextView mLogin;
    @Bind(R.id.edPassword)
    EditText mPassword;
    @Bind(R.id.ivShowPassword)
    ImageButton mShowPassword;
    @Bind(R.id.redAlert)
    RelativeLayout mWrongPasswordAlertView;
    @Bind(R.id.redAlertTextView)
    TextView mWrongDataTextView;
    @Bind(R.id.redAlertButton)
    TextView mCreateAccountButton;
    @Bind(R.id.btnRecoverPassword)
    Button mBtnRecoverPassword;
    private String mEmailForRestorePassword;
    private String mEmailForNewReg;
    private Timer mTimer = new Timer();

    @OnClick(R.id.btnEntrance)
    public void onTFLoginClick() {
        btnTFClick();
        Utils.hideSoftKeyboard(getActivity(), mLogin, mPassword);
    }

    @SuppressWarnings("unused")
    @OnEditorAction(R.id.edPassword)
    public boolean passwordAction(int action) {
        boolean handled = false;
        if (action == EditorInfo.IME_ACTION_DONE) {
            handled = true;
            onTFLoginClick();
        }
        return handled;
    }

    @OnClick({R.id.etMail, R.id.edPassword})
    public void removeRedAlert() {
        if (mWrongPasswordAlertView != null && mWrongPasswordAlertView.getVisibility() == View.VISIBLE) {
            if (isAdded()) {
                mWrongPasswordAlertView.setAnimation(AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_out));
            }
            mWrongPasswordAlertView.setVisibility(View.GONE);
            mWrongDataTextView.setVisibility(View.GONE);
        }
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.redAlertButton)
    public void createAccountClick() {
        EasyTracker.sendEvent("Registration", "StartActivity", "FromAuth", 1L);
        Intent intent = new Intent(getActivity(), RegistrationActivity.class);
        intent.putExtra(RecoverPwdFragment.ARG_EMAIL, mEmailForNewReg);
        startActivityForResult(intent, RegistrationActivity.INTENT_REGISTRATION);
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.btnRecoverPassword)
    public void recoverPasswordClick() {
        Intent intent = new Intent(getActivity(), PasswordRecoverActivity.class);
        intent.putExtra(RecoverPwdFragment.ARG_EMAIL, mEmailForRestorePassword);
        startActivityForResult(intent, PasswordRecoverActivity.INTENT_RECOVER_PASSWORD);
    }

    @Override
    protected int getStatusBarColor() {
        return R.color.status_bar_dark_gray_color;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        setNeedTitles(true);
        View root = inflater.inflate(R.layout.layout_topface_signin, null);
        ButterKnife.bind(this, root);
        initViews(root);
        if (savedInstanceState != null) {
            mLogin.setText(savedInstanceState.getString(RegistrationFragment.EMAIL));
            mPassword.setText(savedInstanceState.getString(RegistrationFragment.PASSWORD));
        }
        return root;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(RegistrationFragment.EMAIL, mLogin.getText().toString());
        outState.putString(RegistrationFragment.PASSWORD, mPassword.getText().toString());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected String getTitle() {
        return getString(R.string.entrance);
    }

    @Override
    public void onResume() {
        super.onResume();
        removeRedAlert();
        mPassword.setText("");
        mPassword.clearFocus();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mTimer.cancel();
    }

    @Override
    protected void initViews(View root) {
        super.initViews(root);
        //fill autocomplete data (emails)
        STAuthMails.initInputField(getActivity(), mLogin);
        mWrongPasswordAlertView = (RelativeLayout) root.findViewById(R.id.redAlert);
        mShowPassword.setOnClickListener(new HidePasswordController(mShowPassword, mPassword));
        mBtnRecoverPassword.setVisibility(View.GONE);
    }

    @Override
    protected int getRootId() {
        return R.id.tf_auth_root;
    }

    @Override
    protected void processAuthError(int codeError, ApiRequest request) {
        super.processAuthError(codeError, request);
        mPassword.setTransformationMethod(new PasswordTransformationMethod());
        switch (codeError) {
            case ErrorCodes.INCORRECT_LOGIN:
            case ErrorCodes.UNKNOWN_SOCIAL_USER:
                redAlert(R.string.incorrect_login);
                //сохранить email введенный при авторизации
                mEmailForNewReg = Utils.getText(mLogin).trim();
                break;
            case ErrorCodes.INCORRECT_PASSWORD:
                redAlert(R.string.incorrect_password);
                mBtnRecoverPassword.setVisibility(View.VISIBLE);
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
        mTFButton.setVisibility(View.GONE);
        mLogin.setVisibility(View.GONE);
        mPassword.setVisibility(View.GONE);
        mShowPassword.setVisibility(View.GONE);
        mBtnRecoverPassword.setVisibility(View.GONE);
    }

    @Override
    protected void hideRetrier() {
        super.hideRetrier();
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
        mBtnRecoverPassword.setVisibility(View.GONE);
        auth(token);
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


    @Override
    protected void showButtons() {
        mTFButton.setVisibility(View.VISIBLE);
        mLogin.setEnabled(true);
        mPassword.setEnabled(true);
        mShowPassword.setEnabled(true);
        mBtnRecoverPassword.setEnabled(true);
    }

    @Override
    protected void hideButtons() {
        mTFButton.setVisibility(View.INVISIBLE);
        mLogin.setEnabled(false);
        mPassword.setEnabled(false);
        mShowPassword.setEnabled(false);
        mBtnRecoverPassword.setEnabled(false);
    }

    @Override
    protected void showProgress() {
        hideButtons();
    }

    @Override
    protected void hideProgress() {
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

    public static class HidePasswordController implements View.OnClickListener {
        private final ImageButton mEye;
        private final EditText mPass;
        private boolean mToggle = false;
        private TransformationMethod mPasswordMethod;

        public HidePasswordController(ImageButton imageButton, EditText editText) {
            this.mEye = imageButton;
            this.mPass = editText;
            mPasswordMethod = new PasswordTransformationMethod();
        }

        @Override
        public void onClick(View v) {
            mToggle = !mToggle;
            mEye.setImageResource(mToggle ? R.drawable.ic_eye_pressed : R.drawable.ic_eye_normal);
            mPass.setTransformationMethod(mToggle ? null : mPasswordMethod);
            Editable text = mPass.getText();
            if (text != null) {
                mPass.setSelection(text.length());
            }
        }
    }
}
