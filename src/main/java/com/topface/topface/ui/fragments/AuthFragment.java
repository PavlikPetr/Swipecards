package com.topface.topface.ui.fragments;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.text.method.TransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.appsflyer.AppsFlyerLib;
import com.google.analytics.tracking.android.EasyTracker;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.Ssid;
import com.topface.topface.Static;
import com.topface.topface.data.Options;
import com.topface.topface.receivers.ConnectionChangeReceiver;
import com.topface.topface.requests.ApiRequest;
import com.topface.topface.requests.AuthRequest;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.handlers.ApiHandler;
import com.topface.topface.requests.handlers.ErrorCodes;
import com.topface.topface.ui.BaseFragmentActivity;
import com.topface.topface.ui.ContainerActivity;
import com.topface.topface.ui.views.RetryViewCreator;
import com.topface.topface.utils.AuthButtonsController;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.Utils;
import com.topface.topface.utils.social.AuthToken;
import com.topface.topface.utils.social.AuthorizationManager;

import java.util.HashSet;
import java.util.Timer;
import java.util.TimerTask;

public class AuthFragment extends BaseFragment {

    public static final String REAUTH_INTENT = "com.topface.topface.action.AUTH";
    public static final String BTNS_HIDDEN = "BtnsHidden";
    public static final String MSG_AUTH_KEY = "msg";
    private RelativeLayout mWrongPasswordAlertView;
    private TextView mWrongDataTextView;
    private TextView mCreateAccountButton;
    private ViewFlipper mAuthViewsFlipper;
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
    private RetryViewCreator mRetryView;
    private boolean mProcessingTFReg = false;
    private Button mOKButton;
    private AuthButtonsController btnsController;
    private LinearLayout mOtherSocialNetworksButton;
    private static final String MAIN_BUTTONS_GA_TAG = "LoginButtonsTest";
    private boolean additionalButtonsScreen = false;
    private boolean hasAuthorized = false;
    private boolean btnsHidden;
    private BroadcastReceiver authorizationReceiver;
    private boolean authReceiverRegistered;
    private boolean mButtonsInitialized = false;
    private ImageView mVkIcon;
    private ImageView mOkIcon;
    private ImageView mFbIcon;

    public static AuthFragment newInstance() {
        return new AuthFragment();
    }

    //пробный пуш
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        Debug.log("AF: onCreate");
        mAuthorizationManager = new AuthorizationManager(getActivity());
        View root = inflater.inflate(R.layout.ac_auth, null);
        if (savedInstanceState != null) {
            btnsHidden = savedInstanceState.getBoolean(BTNS_HIDDEN);
        }
        initViews(root);
        //Если у нас нет токена
        if (!AuthToken.getInstance().isEmpty()) {
            //Если мы попали на этот фрагмент с работающей авторизацией, то просто перезапрашиваем профиль
            loadAllProfileData();
        }
        checkOnline();
        getSupportActionBar().hide();
        return root;
    }

    private void initViews(final View root) {
        mAuthViewsFlipper = (ViewFlipper) root.findViewById(R.id.vfAuthViewFlipper);

        initButtons(root);

        btnsController = new AuthButtonsController(getActivity(), new AuthButtonsController.OnButtonsSettingsLoadedHandler() {
            @Override
            public void buttonSettingsLoaded(HashSet<String> settings) {
                if (btnsController != null) {
                    setAuthInterface();
                }
            }
        });

        initRetryView(root);
        initOtherViews(root);
    }

    private void initAuthorizationHandler() {
        authorizationReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int msg = intent.getIntExtra(MSG_AUTH_KEY, AuthorizationManager.AUTHORIZATION_CANCELLED);
                switch (msg) {
                    case AuthorizationManager.AUTHORIZATION_FAILED:
                        authorizationFailed(ErrorCodes.NETWORK_CONNECT_ERROR, null);
                        break;
                    case AuthorizationManager.DIALOG_COMPLETED:
                        hideButtons();
                        break;
                    case AuthorizationManager.TOKEN_RECEIVED:
                        auth(AuthToken.getInstance());
                        break;
                    case AuthorizationManager.AUTHORIZATION_CANCELLED:
                        showButtons();
                        break;
                }
            }
        };
        authReceiverRegistered = true;
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(authorizationReceiver, new IntentFilter(AuthorizationManager.AUTHORIZATION_TAG));
    }

    private void initButtons(final View root) {
        mVKButton = (Button) root.findViewById(R.id.btnAuthVK);
        mFBButton = (Button) root.findViewById(R.id.btnAuthFB);
        mOKButton = (Button) root.findViewById(R.id.btnAuthOk);

        mOtherSocialNetworksButton = (LinearLayout) root.findViewById(R.id.otherServices);

        mVkIcon = (ImageView) root.findViewById(R.id.vk_ico);
        mOkIcon = (ImageView) root.findViewById(R.id.ok_ico);
        mFbIcon = (ImageView) root.findViewById(R.id.fb_ico);
        mVKButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnVKClick();
            }
        });
        mFBButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnFBClick();
            }
        });
        mOKButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnOKClick();
            }
        });

        mOtherSocialNetworksButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                additionalButtonsScreen = true;
                if (btnsController != null) {
                    btnsController.switchSettings();
                    EasyTracker.getTracker().sendEvent(MAIN_BUTTONS_GA_TAG, "OtherWaysButtonClicked", btnsController.getLocaleTag(), 1L);
                }
                setAuthInterface();
            }
        });

        mSignInView = root.findViewById(R.id.loSignIn);
        mSignInView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentActivity activity = getActivity();
                if (activity != null) {
                    Utils.showSoftKeyboard(activity, mLogin);
                    mAuthViewsFlipper.setDisplayedChild(1);
                }
            }
        });

        mCreateAccountView = root.findViewById(R.id.loCreateAccount);
        mCreateAccountView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EasyTracker.getTracker().sendEvent("Registration", "StartActivity", "FromAuth", 1L);
                Intent intent = new Intent(getActivity(), ContainerActivity.class);
                startActivityForResult(intent, ContainerActivity.INTENT_REGISTRATION_FRAGMENT);
            }
        });

        mTFButton = (Button) root.findViewById(R.id.btnLogin);
        mTFButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnTFClick();
//                removeRedAlert();
                Utils.hideSoftKeyboard(getActivity(), mLogin, mPassword);
            }
        });

        mBackButton = (TextView) root.findViewById(R.id.tvBackToMainAuth);
        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuthViewsFlipper.setDisplayedChild(0);
                removeRedAlert();
                Utils.hideSoftKeyboard(getActivity(), mLogin, mPassword);
            }
        });
        mButtonsInitialized = true;
    }

    private void setAuthInterface() {
        if (btnsController == null || !isAdded()) return;
        if (btnsController.needSN(AuthToken.SN_VKONTAKTE)) {
            mVKButton.setAnimation(AnimationUtils.loadAnimation(getActivity(),
                    R.anim.fade_in));
            mVKButton.setVisibility(View.VISIBLE);
        } else {
            mVKButton.setVisibility(View.GONE);
        }

        if (btnsController.needSN(AuthToken.SN_FACEBOOK)) {
            mFBButton.setAnimation(AnimationUtils.loadAnimation(getActivity(),
                    R.anim.fade_in));
            mFBButton.setVisibility(View.VISIBLE);
        } else {
            mFBButton.setVisibility(View.GONE);
        }

        if (btnsController.needSN(AuthToken.SN_ODNOKLASSNIKI)) {
            mOKButton.setAnimation(AnimationUtils.loadAnimation(getActivity(),
                    R.anim.fade_in));
            mOKButton.setVisibility(View.VISIBLE);
        } else {
            mOKButton.setVisibility(View.GONE);
        }

        HashSet<String> otherSN = btnsController.getOthers();
        if (otherSN.size() == 0) {
            mOtherSocialNetworksButton.setVisibility(View.GONE);
        } else {
            mOtherSocialNetworksButton.setVisibility(View.VISIBLE);
            if (otherSN.contains(AuthToken.SN_VKONTAKTE)) {
                mVkIcon.setVisibility(View.VISIBLE);
            } else {
                mVkIcon.setVisibility(View.GONE);
            }

            if (otherSN.contains(AuthToken.SN_ODNOKLASSNIKI)) {
                mOkIcon.setVisibility(View.VISIBLE);
            } else {
                mOkIcon.setVisibility(View.GONE);
            }

            if (otherSN.contains(AuthToken.SN_FACEBOOK)) {
                mFbIcon.setVisibility(View.VISIBLE);
            } else {
                mFbIcon.setVisibility(View.GONE);
            }
        }
    }

    private void initRetryView(View root) {
        mRetryView = RetryViewCreator.createDefaultRetryView(getActivity(), new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // инициализация обработчика происходит в методе authorizationFailed()
            }
        });
        mRetryView.setVisibility(View.GONE);

        RelativeLayout rootLayout = (RelativeLayout) root.findViewById(R.id.authContainer);
        rootLayout.addView(mRetryView.getView());

        connectionChangeListener = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int mConnectionType = intent.getIntExtra(ConnectionChangeReceiver.CONNECTION_TYPE, -1);
                if (mConnectionType != ConnectionChangeReceiver.CONNECTION_OFFLINE) {
                    if (mRetryView != null) mRetryView.performClick();
                }
            }
        };
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (authorizationReceiver == null || !authReceiverRegistered) {
            initAuthorizationHandler();
        }

        if (mAuthorizationManager != null) {
            hideButtons();
            mAuthorizationManager.onActivityResult(requestCode, resultCode, data);
        }
        if (resultCode == Activity.RESULT_OK &&
                (requestCode == ContainerActivity.INTENT_RECOVER_PASSWORD
                        || requestCode == ContainerActivity.INTENT_REGISTRATION_FRAGMENT)) {
            if (data != null) {
                Bundle extras = data.getExtras();
                if (extras != null) {
                    String login = extras.getString(RegistrationFragment.INTENT_LOGIN);
                    String password = extras.getString(RegistrationFragment.INTENT_PASSWORD);
                    String userId = extras.getString(RegistrationFragment.INTENT_USER_ID);
                    AuthToken.getInstance().saveToken(userId, login, password);
                }
                hideButtons();
                auth(AuthToken.getInstance());
            }
        } else if (resultCode == Activity.RESULT_CANCELED) {
            showButtons();
        }
    }

    private void initOtherViews(View root) {
        mProgressBar = (ProgressBar) root.findViewById(R.id.prsAuthLoading);
        mLoginSendingProgress = (ProgressBar) root.findViewById(R.id.prsLoginSending);
        mWrongPasswordAlertView = (RelativeLayout) root.findViewById(R.id.redAlert);
        mWrongDataTextView = (TextView) root.findViewById(R.id.redAlertTextView);
        mCreateAccountButton = (TextView) root.findViewById(R.id.redAlertButton);
        mCreateAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EasyTracker.getTracker().sendEvent("Registration", "StartActivity", "FromAuth", 1L);
                Intent intent = new Intent(getActivity(), ContainerActivity.class);
                startActivityForResult(intent, ContainerActivity.INTENT_REGISTRATION_FRAGMENT);
            }
        });
        mLogin = (EditText) root.findViewById(R.id.edLogin);
        mPassword = (EditText) root.findViewById(R.id.edPassword);
        root.findViewById(R.id.ivShowPassword).setOnClickListener(new View.OnClickListener() {
            boolean toggle = false;
            TransformationMethod passwordMethod = new PasswordTransformationMethod();

            @Override
            public void onClick(View v) {
                toggle = !toggle;
                mPassword.setTransformationMethod(toggle ? null : passwordMethod);
                Editable text = mPassword.getText();
                if (text != null) {
                    mPassword.setSelection(text.length());
                }
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
        Toast.makeText(App.getContext(), R.string.general_internet_off, Toast.LENGTH_SHORT)
                .show();
    }

    private void auth(final AuthToken token) {
        EasyTracker.getTracker().sendEvent("Profile", "Auth", "FromActivity" + token.getSocialNet(), 1L);
        App.getConfig().onAuthTokenReceived();
        hideButtons();
        final AuthRequest authRequest = new AuthRequest(token.getTokenInfo(), getActivity());
        authRequest.callback(new ApiHandler() {
            @Override
            public void success(IApiResponse response) {
                AuthorizationManager.saveAuthInfo(response);
                if (!token.getSocialNet().equals(AuthToken.SN_TOPFACE)) {
                    btnsController.addSocialNetwork(token.getSocialNet());
                }
                loadAllProfileData();
                hasAuthorized = true;
                //Отправляем статистику в AppsFlyer
                try {
                    AppsFlyerLib.sendTrackingWithEvent(App.getContext(), "registration", "");
                } catch (Exception e) {
                    Debug.error("AppsFlyer Exception", e);
                }
            }

            @Override
            public void fail(final int codeError, IApiResponse response) {
                authorizationFailed(codeError, authRequest);
            }

            public void always(IApiResponse response) {
            }
        }).exec();
    }

    private void loadAllProfileData() {
        hideButtons();
        App.sendProfileAndOptionsRequests(new ApiHandler() {
            @Override
            public void success(IApiResponse response) {
                //После авторизации обязательно бросаем события, что бы профиль загрузился
                LocalBroadcastManager.getInstance(getContext()).sendBroadcast(new Intent(CacheProfile.ACTION_PROFILE_LOAD));
                if (isAdded()) {
                    Utils.hideSoftKeyboard(getActivity(), mLogin, mPassword);
                    ((BaseFragmentActivity) getActivity()).close(AuthFragment.this, true);
                    MenuFragment.selectFragment(FragmentId.F_DATING);
                    LocalBroadcastManager.getInstance(getContext())
                            .sendBroadcast(new Intent(Options.Closing.DATA_FOR_CLOSING_RECEIVED_ACTION));
                }
            }

            @Override
            public void fail(int codeError, IApiResponse response) {
                if (isAdded() && response.isCodeEqual(ErrorCodes.BAN)) {
                    showButtons();
                } else {
                    authorizationFailed(codeError, null);
                    Toast.makeText(App.getContext(), R.string.general_data_error, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(BTNS_HIDDEN, btnsHidden);
    }

    private void authorizationFailed(int codeError, final ApiRequest request) {
        if (!isAdded()) {
            return;
        }
        hideButtons();
        boolean needShowRetry = true;
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append(RetryViewCreator.REFRESH_TEMPLATE).append(getString(R.string.general_dialog_retry));
        switch (codeError) {
            case ErrorCodes.NETWORK_CONNECT_ERROR:
                fillRetryView(getString(R.string.general_reconnect_social), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mRetryView.setVisibility(View.GONE);
                        mProgressBar.setVisibility(View.VISIBLE);
                        resendRequest(request);
                    }
                }, strBuilder.toString());
                break;
            case ErrorCodes.MAINTENANCE:
                fillRetryView(getString(R.string.general_maintenance), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mRetryView.setVisibility(View.GONE);
                        mProgressBar.setVisibility(View.VISIBLE);
                        resendRequest(request);
                    }
                }, strBuilder.toString());
                break;
            case ErrorCodes.CODE_OLD_APPLICATION_VERSION:
                fillRetryView(getString(R.string.general_version_not_supported), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Utils.goToMarket(getActivity());
                    }
                }, getString(R.string.popup_version_update));
                break;
            case ErrorCodes.INCORRECT_LOGIN:
            case ErrorCodes.UNKNOWN_SOCIAL_USER:
                redAlert(R.string.incorrect_login);
                needShowRetry = false;
                break;
            case ErrorCodes.INCORRECT_PASSWORD:
                redAlert(R.string.incorrect_password);
                mRecoverPwd.setVisibility(View.VISIBLE);
                needShowRetry = false;
                break;
            case ErrorCodes.MISSING_REQUIRE_PARAMETER:
                redAlert(R.string.empty_fields);
                needShowRetry = false;
                break;
            case ErrorCodes.USER_DELETED:
                needShowRetry = false;
                break;
            case ErrorCodes.WRONG_RESPONSE:
            default:
                mAuthViewsFlipper.setVisibility(View.GONE);
                fillRetryView(getString(R.string.general_data_error), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mRetryView.setVisibility(View.GONE);
                        mAuthViewsFlipper.setVisibility(View.VISIBLE);
                        mProgressBar.setVisibility(View.VISIBLE);
                        resendRequest(request);
                    }
                }, strBuilder.toString());
                break;
        }

        if (needShowRetry) {
            mRetryView.setVisibility(View.VISIBLE);
            mProgressBar.setVisibility(View.GONE);
        } else {
            showButtons();
        }
    }

    private void fillRetryView(String text, View.OnClickListener listener, String btnText) {
        mRetryView.setText(text);
        mRetryView.setButtonText(btnText);
        mRetryView.setListener(listener);
    }

    private void resendRequest(ApiRequest request) {
        if (request != null) {
            request.canceled = false;
            request.exec();
        } else {
            //Если запрос базовой информации не прошел, то повторяем запрос
            loadAllProfileData();
        }
    }

    private void redAlert(int resId) {
        redAlert(getString(resId));
    }

    private void redAlert(String text) {
        if (mWrongPasswordAlertView != null && mAuthViewsFlipper.getDisplayedChild() == 1) {
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
    }

    private void removeRedAlert() {
        FragmentActivity activity = getActivity();
        if (activity != null && mWrongPasswordAlertView != null && mWrongPasswordAlertView.getVisibility() == View.VISIBLE) {
            mWrongPasswordAlertView.setAnimation(AnimationUtils.loadAnimation(activity, android.R.anim.fade_out));
            mWrongPasswordAlertView.setVisibility(View.GONE);
            mWrongDataTextView.setVisibility(View.GONE);
        }
    }


    private void showButtons() {
        //Эта проверка нужна, для безопасной работы в
        btnsHidden = false;
        if (mFBButton != null && mVKButton != null && mProgressBar != null) {
            if (btnsController.needSN(AuthToken.SN_FACEBOOK)) {
                mFBButton.setVisibility(View.VISIBLE);
            }
            if (btnsController.needSN(AuthToken.SN_VKONTAKTE)) {
                mVKButton.setVisibility(View.VISIBLE);
            }
            if (btnsController.needSN(AuthToken.SN_ODNOKLASSNIKI)) {
                mOKButton.setVisibility(View.VISIBLE);
            }
            if (btnsController.getOthers().size() > 0) {
                mOtherSocialNetworksButton.setVisibility(View.VISIBLE);
            }
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

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    private void hideButtons() {
        if (mButtonsInitialized && isAdded()) {
            btnsHidden = true;
            mFBButton.setVisibility(View.GONE);
            mVKButton.setVisibility(View.GONE);
            mOKButton.setVisibility(View.GONE);
            mOtherSocialNetworksButton.setVisibility(View.GONE);
            mSignInView.setVisibility(View.GONE);
            mCreateAccountView.setVisibility(View.GONE);
            mRetryView.setVisibility(View.GONE);
            mTFButton.setVisibility(View.INVISIBLE);
            if (mProcessingTFReg) {
                mLoginSendingProgress.setVisibility(View.VISIBLE);
            } else {
                mProgressBar.setVisibility(View.VISIBLE);
            }
            mRecoverPwd.setEnabled(false);
            mLogin.setEnabled(false);
            mPassword.setEnabled(false);
            mBackButton.setEnabled(false);
        }
    }


    private void btnVKClick() {
        // костыль, надо избавить от viewflipper к чертовой бабушке
        mProcessingTFReg = false;
        EasyTracker.getTracker().sendEvent(MAIN_BUTTONS_GA_TAG, additionalButtonsScreen ? "LoginAdditionalVk" : "LoginMainVk", btnsController.getLocaleTag(), 1L);

        if (checkOnline() && mAuthorizationManager != null) {
            hideButtons();
            mAuthorizationManager.vkontakteAuth();
        }
    }

    private void btnFBClick() {
        // костыль, надо избавить от viewflipper к чертовой бабушке
        mProcessingTFReg = false;
        EasyTracker.getTracker().sendEvent(MAIN_BUTTONS_GA_TAG, additionalButtonsScreen ? "LoginAdditionalFb" : "LoginMainFb", btnsController.getLocaleTag(), 1L);
        if (checkOnline() && mAuthorizationManager != null) {
            hideButtons();
            mAuthorizationManager.facebookAuth();
        }
    }


    private void btnOKClick() {
        mProcessingTFReg = false;
        EasyTracker.getTracker().sendEvent(MAIN_BUTTONS_GA_TAG, additionalButtonsScreen ? "LoginAdditionalOk" : "LoginMainOk", btnsController.getLocaleTag(), 1L);
        if (checkOnline() && mAuthorizationManager != null) {
            mAuthorizationManager.odnoklassnikiAuth(new AuthorizationManager.OnTokenReceivedListener() {
                @Override
                public void onTokenReceived() {
                    hideButtons();
                }
            });
        }
    }

    private void btnTFClick() {
        // костыль, надо избавить от viewflipper к чертовой бабушке
        mProcessingTFReg = true;
        //---------------------------------------------------------
        if (checkOnline()) {
            hideButtons();
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
            auth(token);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(connectionChangeListener,
                new IntentFilter(ConnectionChangeReceiver.REAUTH));
        if (authorizationReceiver == null || !authReceiverRegistered) {
            initAuthorizationHandler();
        }
        removeRedAlert();
        if (Ssid.isLoaded() && !AuthToken.getInstance().isEmpty()) {
            loadAllProfileData();
        } else {
            showButtons();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        authReceiverRegistered = false;
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(authorizationReceiver);
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(connectionChangeListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getSupportActionBar().show();
        if (!hasAuthorized) {
            EasyTracker.getTracker().sendEvent(MAIN_BUTTONS_GA_TAG, additionalButtonsScreen ? "DismissAdditional" : "DismissMain", btnsController.getLocaleTag(), 1L);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    protected String getTitle() {
        return getString(R.string.app_name);
    }
}
