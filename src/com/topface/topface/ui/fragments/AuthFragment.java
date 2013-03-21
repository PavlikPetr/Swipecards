package com.topface.topface.ui.fragments;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.text.method.PasswordTransformationMethod;
import android.text.method.TransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import com.google.analytics.tracking.android.EasyTracker;
import com.topface.topface.*;
import com.topface.topface.data.Auth;
import com.topface.topface.data.Options;
import com.topface.topface.data.Profile;
import com.topface.topface.receivers.ConnectionChangeReceiver;
import com.topface.topface.requests.*;
import com.topface.topface.requests.handlers.ApiHandler;
import com.topface.topface.ui.BaseFragmentActivity;
import com.topface.topface.ui.ContainerActivity;
import com.topface.topface.ui.views.IllustratedTextView;
import com.topface.topface.ui.views.RetryView;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Utils;
import com.topface.topface.utils.social.AuthToken;
import com.topface.topface.utils.social.AuthorizationManager;

import java.util.Timer;
import java.util.TimerTask;

public class AuthFragment extends BaseFragment {

    private TextView mWrongPasswordAlertView;
    private ViewFlipper mAuthViewsFlipper;
    private RetryView mRetryView;
    private Button mFBButton;
    private Button mVKButton;
    private Button mTFButton;
    private View mSignInView;
    private View mCreateAccountView;
    private TextView mRecoverPwd;
    private EditText mLogin;
    private EditText mPassword;
    private ProgressBar mProgressBar;
    private ProgressBar mLoginSendingProgress;
    private AuthorizationManager mAuthorizationManager;
    private BroadcastReceiver connectionChangeListener;
    private TextView mBackButton;
    private Timer mTimer = new Timer();

    public static AuthFragment newInstance() {
        return new AuthFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.ac_auth, null);
        initViews(root);
        //Если у нас нет токена
        if (AuthToken.getInstance().isEmpty()) {
            initAuthorizationHandler();
        } else {
            //Если мы попали на этот фрагмент с работающей авторизацией, то просто перезапрашиваем профиль
            hideButtons();
            getProfileAndOptions();
        }
        checkOnline();
        return root;
    }

    private void initViews(View root) {
        mAuthViewsFlipper = (ViewFlipper) root.findViewById(R.id.vfAuthViewFlipper);
        initButtons(root);
        initRetryView(root);
        initOtherViews(root);
    }

    private void initAuthorizationHandler() {
        mAuthorizationManager = new AuthorizationManager(getActivity());
        mAuthorizationManager.setOnAuthorizationHandler(new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case AuthorizationManager.AUTHORIZATION_FAILED:
                        authorizationFailed(ApiResponse.NETWORK_CONNECT_ERROR, null);
                        break;
                    case AuthorizationManager.DIALOG_COMPLETED:
                        hideButtons();
                        break;
                    case AuthorizationManager.TOKEN_RECEIVED:
                        auth(generateAuthRequest((AuthToken) msg.obj));
                        break;
                    case AuthorizationManager.AUTHORIZATION_CANCELLED:
                        showButtons();
                        break;
                    default:
                        super.handleMessage(msg);
                }
            }
        });
    }

    private void initButtons(View root) {
        mVKButton = (Button) root.findViewById(R.id.btnAuthVK);
        mVKButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnVKClick();
            }
        });

        mFBButton = (Button) root.findViewById(R.id.btnAuthFB);
        mFBButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnFBClick();
            }
        });

        mSignInView = root.findViewById(R.id.loSignIn);
        mSignInView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuthViewsFlipper.setDisplayedChild(1);
                mLogin.requestFocus();
                Utils.showSoftKeyboard(getActivity(), mLogin);
            }
        });

        mCreateAccountView = root.findViewById(R.id.loCreateAccount);
        mCreateAccountView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EasyTracker.getTracker().trackEvent("Registration", "StartActivity", "FromAuth", 1L);
                Intent intent = new Intent(getActivity(), ContainerActivity.class);
                startActivityForResult(intent, ContainerActivity.INTENT_REGISTRATION_FRAGMENT);
            }
        });

        mTFButton = (Button) root.findViewById(R.id.btnLogin);
        mTFButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnTFClick();
                removeRedAlert();
                hideSoftKeyboard();
            }
        });

        mBackButton = (TextView) root.findViewById(R.id.tvBackToMainAuth);
        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuthViewsFlipper.setDisplayedChild(0);
                removeRedAlert();
                hideSoftKeyboard();
            }
        });
    }


    private void hideSoftKeyboard() {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (mLogin != null) {
            imm.hideSoftInputFromWindow(mLogin.getWindowToken(), 0);
        }

        if (mPassword != null) {
            imm.hideSoftInputFromWindow(mPassword.getWindowToken(), 0);
        }
    }

    private void initRetryView(View root) {
        mRetryView = new RetryView(getActivity());
        mRetryView.setErrorMsg(getString(R.string.general_data_error));
        mRetryView.addButton(RetryView.REFRESH_TEMPLATE + getString(R.string.general_dialog_retry), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // инициализация обработчика происходит в методе authorizationFailed()
            }
        });
        mRetryView.setVisibility(View.GONE);

        RelativeLayout authContainer = (RelativeLayout) root.findViewById(R.id.authContainer);

        authContainer.addView(mRetryView);
        connectionChangeListener = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int mConnectionType = intent.getIntExtra(ConnectionChangeReceiver.CONNECTION_TYPE, -1);

                if (mConnectionType != ConnectionChangeReceiver.CONNECTION_OFFLINE) {
                    IllustratedTextView btn = mRetryView.getBtn1();
                    if (btn != null) {
                        btn.performClick();
                    }
                }
            }
        };
    }

    @Override
    public void
    onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (mAuthorizationManager != null) mAuthorizationManager.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK &&
                (requestCode == ContainerActivity.INTENT_RECOVER_PASSWORD
                        || requestCode == ContainerActivity.INTENT_REGISTRATION_FRAGMENT)) {
            if (data != null) {
                Bundle extras = data.getExtras();
                String login = extras.getString(RegistrationFragment.INTENT_LOGIN);
                String password = extras.getString(RegistrationFragment.INTENT_PASSWORD);
                String userId = extras.getString(RegistrationFragment.INTENT_USER_ID);
                AuthToken.getInstance().saveToken(userId, login, password);
                hideButtons();
                auth(generateTopfaceAuthRequest(login, password));
            }
        } else if (resultCode == Activity.RESULT_CANCELED) {
            showButtons();
        }
    }

    private void initOtherViews(View root) {
        mProgressBar = (ProgressBar) root.findViewById(R.id.prsAuthLoading);
        mLoginSendingProgress = (ProgressBar) root.findViewById(R.id.prsLoginSending);
        mWrongPasswordAlertView = (TextView) root.findViewById(R.id.tvRedAlert);
        mLogin = (EditText) root.findViewById(R.id.edLogin);
        mPassword = (EditText) root.findViewById(R.id.edPassword);
        root.findViewById(R.id.ivShowPassword).setOnClickListener(new View.OnClickListener() {
            boolean toggle = false;
            TransformationMethod passwordMethod = new PasswordTransformationMethod();

            @Override
            public void onClick(View v) {
                toggle = !toggle;
                mPassword.setTransformationMethod(toggle ? null : passwordMethod);
                mPassword.setSelection(mPassword.getText().length());
            }
        });
        mRecoverPwd = (TextView) root.findViewById(R.id.tvRecoverPwd);
        mRecoverPwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ContainerActivity.class);
                startActivityForResult(intent, ContainerActivity.INTENT_RECOVER_PASSWORD);
            }
        });
        mRecoverPwd.setVisibility(View.GONE);
    }

    private boolean checkOnline() {
        if (!App.isOnline()) {
            showNoInternetToast();
            return false;
        }
        return true;
    }

    private void showNoInternetToast() {
        Toast.makeText(getActivity(), getString(R.string.general_internet_off), Toast.LENGTH_SHORT)
                .show();
    }

    private void auth(final AuthRequest authRequest) {
        authRequest.callback(new ApiHandler() {
            @Override
            public void success(ApiResponse response) {
                saveAuthInfo(response);
                getProfileAndOptions();
            }

            @Override
            public void fail(final int codeError, ApiResponse response) {
                authorizationFailed(codeError, authRequest);
            }

            @Override
            public void cancel() {
                showButtons();
            }
        }).exec();
    }

    private AuthRequest generateAuthRequest(AuthToken token) {
        AuthRequest authRequest = new AuthRequest(token, getActivity());
        registerRequest(authRequest);
        EasyTracker.getTracker().trackEvent("Profile", "Auth", "FromActivity" + token.getSocialNet(), 1L);

        return authRequest;
    }

    private AuthRequest generateTopfaceAuthRequest(final String login, final String password) {
        final AuthRequest authRequest = new AuthRequest(login, password, getActivity());
        registerRequest(authRequest);
        authRequest.callback(new ApiHandler() {
            @Override
            public void success(ApiResponse response) {
                saveAuthInfo(response);
                getProfileAndOptions(new ProfileIdReceiver() {
                    @Override
                    public void onProfileIdReceived(int profileId) {
                        AuthToken.getInstance().saveToken(Integer.toString(profileId), login, password);
                    }
                });
            }

            @Override
            public void fail(final int codeError, ApiResponse response) {
                authorizationFailed(codeError, authRequest);
            }

            @Override
            public void cancel() {
//                showButtons();
            }
        });
        EasyTracker.getTracker().trackEvent("Profile", "Auth", "FromActivity" + AuthToken.SN_TOPFACE, 1L);

        return authRequest;
    }

    private void saveAuthInfo(ApiResponse response) {
        Auth auth = new Auth(response);
        Ssid.save(auth.ssid);
        GCMUtils.init(getActivity());
    }

    private void getProfileAndOptions() {
        getProfileAndOptions(null);
    }

    private void getProfileAndOptions(final ProfileIdReceiver idReceiver) {
        final ProfileRequest profileRequest = new ProfileRequest(getActivity());
        profileRequest.part = ProfileRequest.P_ALL;
        registerRequest(profileRequest);
        profileRequest.callback(new DataApiHandler<Profile>() {

            @Override
            protected void success(Profile data, ApiResponse response) {
                CacheProfile.setProfile(data, response);
                if (idReceiver != null) idReceiver.onProfileIdReceived(CacheProfile.getProfile().uid);
                getOptions();
            }

            @Override
            protected Profile parseResponse(ApiResponse response) {
                return Profile.parse(response);
            }

            @Override
            public void fail(int codeError, ApiResponse response) {
                if (response.code == ApiResponse.BAN)
                    showButtons();
                else {
                    authorizationFailed(codeError, profileRequest);
                    Toast.makeText(getActivity(), getString(R.string.general_data_error),
                            Toast.LENGTH_SHORT).show();
                }
            }
        }).exec();
    }

    private void getOptions() {
        final OptionsRequest request = new OptionsRequest(getActivity());
        registerRequest(request);
        request.callback(new ApiHandler() {
            @Override
            public void success(final ApiResponse response) {
                Options.parse(response);
                Utils.hideSoftKeyboard(getActivity(), mLogin, mPassword);
                ((BaseFragmentActivity) getActivity()).close(AuthFragment.this);
            }

            @Override
            public void fail(int codeError, ApiResponse response) {
                if (response.code == ApiResponse.BAN)
                    showButtons();
                else {
                    request.callback(this);
                    authorizationFailed(codeError, request);
                    Toast.makeText(getActivity(), getString(R.string.general_data_error),
                            Toast.LENGTH_SHORT).show();
                }
            }
        }).exec();
    }

    private void authorizationFailed(int codeError, final ApiRequest request) {
        hideButtons();
        boolean needShowRetry = true;

        switch (codeError) {
            case IApiResponse.NETWORK_CONNECT_ERROR:
                mRetryView.setErrorMsg(getString(R.string.general_reconnect_social));
                mRetryView.setTextToButton1(RetryView.REFRESH_TEMPLATE + getString(R.string.general_dialog_retry));
                mRetryView.setListenerToBtn(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mRetryView.setVisibility(View.GONE);
                        mProgressBar.setVisibility(View.VISIBLE);
                        resendRequest(request);
                    }
                });
                break;
            case IApiResponse.MAINTENANCE:
                mRetryView.setErrorMsg(getString(R.string.general_maintenance));
                mRetryView.setTextToButton1(RetryView.REFRESH_TEMPLATE + getString(R.string.general_dialog_retry));
                mRetryView.setListenerToBtn(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mRetryView.setVisibility(View.GONE);
                        mProgressBar.setVisibility(View.VISIBLE);
                        resendRequest(request);
                    }
                });
                break;
            case IApiResponse.CODE_OLD_APPLICATION_VERSION:
                mRetryView.setErrorMsg(getString(R.string.general_version_not_supported));
                mRetryView.setTextToButton1(getString(R.string.popup_version_update));
                mRetryView.setListenerToBtn(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Utils.goToMarket(getActivity());
                    }
                });
                break;
            case IApiResponse.INCORRECT_LOGIN:
            case IApiResponse.UNKNOWN_SOCIAL_USER:
                redAlert(R.string.incorrect_login);
                needShowRetry = false;
                break;
            case IApiResponse.INCORRECT_PASSWORD:
                redAlert(R.string.incorrect_password);
                mRecoverPwd.setVisibility(View.VISIBLE);
                needShowRetry = false;
                break;
            case IApiResponse.MISSING_REQUIRE_PARAMETER:
                redAlert(R.string.empty_fields);
                needShowRetry = false;
                break;
            default:
                mAuthViewsFlipper.setVisibility(View.GONE);
                mRetryView.setErrorMsg(getString(R.string.general_data_error));
                mRetryView.setListenerToBtn(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mRetryView.setVisibility(View.GONE);
                        mAuthViewsFlipper.setVisibility(View.VISIBLE);
                        mProgressBar.setVisibility(View.VISIBLE);
                        resendRequest(request);
                    }
                });
                break;
        }

        if ((request != null) && needShowRetry) {
            mRetryView.setVisibility(View.VISIBLE);
            mProgressBar.setVisibility(View.GONE);
        } else {
            showButtons();
        }
    }

    private void resendRequest(ApiRequest request) {
        if (request != null) {
            request.canceled = false;
            registerRequest(request);
            request.exec();
        }
    }

    private void redAlert(int resId) {
        redAlert(getString(resId));
    }

    private void redAlert(String text) {
        if (mWrongPasswordAlertView != null && mAuthViewsFlipper.getDisplayedChild() == 1) {
            if (text != null) {
                mWrongPasswordAlertView.setText(text);
            }
            mWrongPasswordAlertView.setAnimation(AnimationUtils.loadAnimation(getActivity().getApplicationContext(),
                    R.anim.slide_down_fade_in));
            mWrongPasswordAlertView.setVisibility(View.VISIBLE);
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
            }, Static.RED_ALERT_APPEARANCE_TIME);
        }
    }

    private void removeRedAlert() {
        if (mWrongPasswordAlertView != null && mWrongPasswordAlertView.getVisibility() == View.VISIBLE) {
            mWrongPasswordAlertView.setAnimation(AnimationUtils.loadAnimation(getActivity().getApplicationContext(),
                    android.R.anim.fade_out));
            mWrongPasswordAlertView.setVisibility(View.INVISIBLE);
        }
    }

    private void showButtons() {
        //Эта проверка нужна, для безопасной работы в
        if (mFBButton != null && mVKButton != null && mProgressBar != null) {
            mFBButton.setVisibility(View.VISIBLE);
            mVKButton.setVisibility(View.VISIBLE);
            mSignInView.setVisibility(View.VISIBLE);
            mTFButton.setVisibility(View.VISIBLE);
            mCreateAccountView.setVisibility(View.VISIBLE);
            mProgressBar.setVisibility(View.GONE);
            mLoginSendingProgress.setVisibility(View.GONE);
            mRetryView.setVisibility(View.GONE);
            mRecoverPwd.setEnabled(true);
            mLogin.setEnabled(true);
            mPassword.setEnabled(true);
            mBackButton.setEnabled(true);
        }
    }

    private void hideButtons() {
        mFBButton.setVisibility(View.GONE);
        mVKButton.setVisibility(View.GONE);
        mSignInView.setVisibility(View.GONE);
        mCreateAccountView.setVisibility(View.GONE);
        mRetryView.setVisibility(View.GONE);
        mTFButton.setVisibility(View.INVISIBLE);
        mProgressBar.setVisibility(View.VISIBLE);
        mLoginSendingProgress.setVisibility(View.VISIBLE);
        mRecoverPwd.setEnabled(false);
        mLogin.setEnabled(false);
        mPassword.setEnabled(false);
        mBackButton.setEnabled(false);
    }

    private void btnVKClick() {
        if (checkOnline() && mAuthorizationManager != null) {
            hideButtons();
            mAuthorizationManager.vkontakteAuth();
        }
//
    }

    private void btnFBClick() {
        if (checkOnline() && mAuthorizationManager != null) {
            hideButtons();
            mAuthorizationManager.facebookAuth();
        }
    }

    private void btnTFClick() {
        if (checkOnline()) {
            hideButtons();
            String login = mLogin.getText().toString();
            String password = mPassword.getText().toString();
            AuthRequest authRequest = generateTopfaceAuthRequest(login, password);
            authRequest.exec();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(connectionChangeListener,
                new IntentFilter(ConnectionChangeReceiver.REAUTH));
        removeRedAlert();
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(connectionChangeListener);
    }

    interface ProfileIdReceiver {
        void onProfileIdReceived(int id);
    }


}


