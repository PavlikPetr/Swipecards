package com.topface.topface.ui.fragments;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
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

import com.google.analytics.tracking.android.EasyTracker;
import com.topface.topface.App;
import com.topface.topface.GCMUtils;
import com.topface.topface.R;
import com.topface.topface.Ssid;
import com.topface.topface.Static;
import com.topface.topface.data.Auth;
import com.topface.topface.data.Options;
import com.topface.topface.data.Profile;
import com.topface.topface.receivers.ConnectionChangeReceiver;
import com.topface.topface.requests.ApiRequest;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.AppOptionsRequest;
import com.topface.topface.requests.AuthRequest;
import com.topface.topface.requests.DataApiHandler;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.ProfileRequest;
import com.topface.topface.requests.handlers.ApiHandler;
import com.topface.topface.requests.handlers.ErrorCodes;
import com.topface.topface.ui.BaseFragmentActivity;
import com.topface.topface.ui.ContainerActivity;
import com.topface.topface.ui.NavigationActivity;
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater,container,savedInstanceState);
        Debug.log("AF: onCreate");
        mAuthorizationManager = new AuthorizationManager(getActivity());
        Activity activity = getActivity();
        if (activity instanceof NavigationActivity) {
            ((NavigationActivity) activity).setMenuEnabled(false);
        }
        View root = inflater.inflate(R.layout.ac_auth, null);
        if (savedInstanceState != null) {
            btnsHidden = savedInstanceState.getBoolean(BTNS_HIDDEN);
        }
        initViews(root);
        //Если у нас нет токена
        if (!AuthToken.getInstance().isEmpty()) {
            //Если мы попали на этот фрагмент с работающей авторизацией, то просто перезапрашиваем профиль
            hideButtons();
            getProfileAndOptions();
        }
        checkOnline();
        getSupportActionBar().hide();
        return root;
    }

    private void initViews(View root) {
        mAuthViewsFlipper = (ViewFlipper) root.findViewById(R.id.vfAuthViewFlipper);
        initButtons(root);
        initRetryView(root);
        initOtherViews(root);
//        if (btnsHidden && getActivity() != null) {
//            hideButtons();
//        } else {
//            showButtons();
//        }
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
                        auth(generateAuthRequest((AuthToken) intent.getParcelableExtra("token")));
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
        btnsController = new AuthButtonsController(getActivity(), new AuthButtonsController.OnButtonsSettingsLoadedListener() {
            @Override
            public void buttonSettingsLoaded(HashSet<String> settings) {
                initButtonsWithSettings(root);
            }
        });
    }

    private void initButtonsWithSettings(View root) {

        mVKButton = (Button) root.findViewById(R.id.btnAuthVK);
        mFBButton = (Button) root.findViewById(R.id.btnAuthFB);
        mOKButton = (Button) root.findViewById(R.id.btnAuthOk);

        mOtherSocialNetworksButton = (LinearLayout) root.findViewById(R.id.otherServices);

        mVkIcon = (ImageView) root.findViewById(R.id.vk_ico);
        mOkIcon =  (ImageView)root.findViewById(R.id.ok_ico);
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
                EasyTracker.getTracker().sendEvent(MAIN_BUTTONS_GA_TAG, "OtherWaysButtonClicked", btnsController.getLocaleTag(), 1L);
                additionalButtonsScreen = true;
                btnsController.switchSettings();
                setAuthInterface();
            }
        });

        setAuthInterface();

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
        if (btnsController.needSN(AuthToken.SN_VKONTAKTE)) {
            mVKButton.setAnimation(AnimationUtils.loadAnimation(getActivity().getApplicationContext(),
                    R.anim.fade_in));
            mVKButton.setVisibility(View.VISIBLE);
        } else {
            mVKButton.setVisibility(View.GONE);
        }

        if (btnsController.needSN(AuthToken.SN_FACEBOOK)) {
            mFBButton.setAnimation(AnimationUtils.loadAnimation(getActivity().getApplicationContext(),
                    R.anim.fade_in));
            mFBButton.setVisibility(View.VISIBLE);
        } else {
            mFBButton.setVisibility(View.GONE);
        }

        if (btnsController.needSN(AuthToken.SN_ODNOKLASSNIKI)) {
            mOKButton.setAnimation(AnimationUtils.loadAnimation(getActivity().getApplicationContext(),
                    R.anim.fade_in));
            mOKButton.setVisibility(View.VISIBLE);
        } else {
            mOKButton.setVisibility(View.GONE);
        }


        HashSet<String> otherSN = btnsController.getOhters();
        if (otherSN.size() == 0) {
            mOtherSocialNetworksButton.setVisibility(View.GONE);
        } else {
            mOtherSocialNetworksButton.setVisibility(View.VISIBLE);
            if (btnsController.getOhters().contains(AuthToken.SN_VKONTAKTE)) {
                mVkIcon.setVisibility(View.VISIBLE);
            } else {
                mVkIcon.setVisibility(View.GONE);
            }

            if (btnsController.getOhters().contains(AuthToken.SN_ODNOKLASSNIKI)) {
                mOkIcon.setVisibility(View.VISIBLE);
            } else {
                mOkIcon.setVisibility(View.GONE);
            }

            if (btnsController.getOhters().contains(AuthToken.SN_FACEBOOK)) {
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
                String login = extras.getString(RegistrationFragment.INTENT_LOGIN);
                String password = extras.getString(RegistrationFragment.INTENT_PASSWORD);
                String userId = extras.getString(RegistrationFragment.INTENT_USER_ID);
                AuthToken.getInstance().saveToken(userId, login, password);
                hideButtons();
                auth(generateTopfaceAuthRequest(AuthToken.getInstance()));
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
        Toast.makeText(App.getContext(), R.string.general_internet_off, Toast.LENGTH_SHORT)
                .show();
    }

    private void auth(final AuthRequest authRequest) {
        authRequest.callback(new ApiHandler() {
            @Override
            public void success(IApiResponse response) {
                saveAuthInfo((ApiResponse) response);
                btnsController.addSocialNetwork(AuthToken.getInstance().getSocialNet());
                getProfileAndOptions();
                hasAuthorized = true;
            }

            @Override
            public void fail(final int codeError, IApiResponse response) {
                authorizationFailed(codeError, authRequest);
            }

            public void always(IApiResponse response) {
            }
        }).exec();
    }

    private AuthRequest generateAuthRequest(AuthToken token) {
        AuthRequest authRequest = new AuthRequest(token, getActivity());
        registerRequest(authRequest);
        EasyTracker.getTracker().sendEvent("Profile", "Auth", "FromActivity" + token.getSocialNet(), 1L);
        return authRequest;
    }

    private AuthRequest generateTopfaceAuthRequest(AuthToken token) {
        final AuthRequest authRequest = new AuthRequest(token, getActivity());
        final String login = token.getLogin();
        final String password = token.getPassword();
        registerRequest(authRequest);
        authRequest.callback(new ApiHandler() {
            @Override
            public void success(IApiResponse response) {
                saveAuthInfo((ApiResponse) response);
                getProfileAndOptions(new ProfileIdReceiver() {
                    @Override
                    public void onProfileIdReceived(int profileId) {
                        AuthToken.getInstance().saveToken(Integer.toString(profileId), login, password);
                    }
                });
            }

            @Override
            public void fail(final int codeError, IApiResponse response) {
                authorizationFailed(codeError, authRequest);
            }

        });
        EasyTracker.getTracker().sendEvent("Profile", "Auth", "FromActivity" + AuthToken.SN_TOPFACE, 1L);

        return authRequest;
    }

    private void saveAuthInfo(ApiResponse response) {
        Auth auth = new Auth(response);
        Ssid.save(auth.ssid);
        GCMUtils.init(App.getContext());
    }

    private void getProfileAndOptions() {
        getProfileAndOptions(null);
    }

    private void getProfileAndOptions(final ProfileIdReceiver idReceiver) {
        final ProfileRequest profileRequest = new ProfileRequest(getActivity());
        profileRequest.part = ProfileRequest.P_ALL;
        registerRequest(profileRequest);
        hideButtons();
        profileRequest.callback(new DataApiHandler<Profile>() {

            @Override
            protected void success(Profile data, IApiResponse response) {
                CacheProfile.setProfile(data, (ApiResponse) response);
                if (idReceiver != null) idReceiver.onProfileIdReceived(CacheProfile.uid);
                getOptions();
            }

            @Override
            protected Profile parseResponse(ApiResponse response) {
                return Profile.parse(response);
            }

            @Override
            public void fail(int codeError, IApiResponse response) {
                if (response.isCodeEqual(ErrorCodes.BAN))
                    showButtons();
                else {
                    authorizationFailed(codeError, profileRequest);
                    Toast.makeText(App.getContext(), R.string.general_data_error, Toast.LENGTH_SHORT).show();
                }
            }
        }).exec();
    }

    private void getOptions() {
        final AppOptionsRequest request = new AppOptionsRequest(getActivity());
        registerRequest(request);
        if (isAdded()) {
            hideButtons();
        }
        request.callback(new DataApiHandler<Options>() {
            @Override
            protected void success(Options data, IApiResponse response) {
                Utils.hideSoftKeyboard(getActivity(), mLogin, mPassword);
                ((BaseFragmentActivity) getActivity()).close(AuthFragment.this, true);
                LocalBroadcastManager.getInstance(getContext())
                        .sendBroadcast(new Intent(Options.Closing.DATA_FOR_CLOSING_RECEIVED_ACTION));
            }

            @Override
            protected Options parseResponse(ApiResponse response) {
                return Options.parse(response);
            }

            @Override
            public void fail(int codeError, IApiResponse response) {
                if (response.isCodeEqual(ErrorCodes.BAN))
                    showButtons();
                else {
                    request.callback(this);
                    authorizationFailed(codeError, request);
                    Context context = App.getContext();
                    Toast.makeText(context, context.getString(R.string.general_data_error),
                            Toast.LENGTH_SHORT).show();
                }
            }
        }).exec();
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
        if (isAdded()) {
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

            if ((request != null) && needShowRetry) {
                mRetryView.setVisibility(View.VISIBLE);
                mProgressBar.setVisibility(View.GONE);
            } else {
                showButtons();
            }
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
                mWrongDataTextView.setText(text);
            }
            mWrongPasswordAlertView.setAnimation(AnimationUtils.loadAnimation(getActivity().getApplicationContext(),
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
            if (btnsController.getOhters().size() > 0) {
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
        if (mButtonsInitialized) {
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
            hideButtons();
            mAuthorizationManager.odnoklassnikiAuth();
        }
    }

    private void btnTFClick() {
        // костыль, надо избавить от viewflipper к чертовой бабушке
        mProcessingTFReg = true;
        //---------------------------------------------------------
        if (checkOnline()) {
            hideButtons();
            String login = mLogin.getText().toString();
            String password = mPassword.getText().toString();
            if (TextUtils.isEmpty(login.trim()) || TextUtils.isEmpty(password.trim())) {
                redAlert(R.string.empty_fields);
                showButtons();
                return;
            } else if (!Utils.isValidEmail(login)) {
                redAlert(R.string.incorrect_login);
                showButtons();
                return;
            }
            AuthToken.getInstance().saveToken("", login, password);
            AuthRequest authRequest = generateTopfaceAuthRequest(AuthToken.getInstance());
            authRequest.exec();
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

    interface ProfileIdReceiver {
        void onProfileIdReceived(int id);
    }

    @Override
    protected String getTitle() {
        return getString(R.string.app_name);
    }
}
