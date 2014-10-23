package com.topface.topface.ui.fragments;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.topface.framework.utils.Debug;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.Ssid;
import com.topface.topface.requests.handlers.ErrorCodes;
import com.topface.topface.ui.PasswordRecoverActivity;
import com.topface.topface.ui.RegistrationActivity;
import com.topface.topface.utils.AuthButtonsController;
import com.topface.topface.utils.EasyTracker;
import com.topface.topface.utils.social.AuthToken;
import com.topface.topface.utils.social.AuthorizationManager;
import com.topface.topface.utils.social.Authorizer;

import java.util.HashSet;

public class AuthFragment extends BaseAuthFragment {

    public static final String REAUTH_INTENT = "com.topface.topface.action.AUTH";
    public static final String BTNS_HIDDEN = "BtnsHidden";

    private Button mFBButton;
    private Button mVKButton;
    private View mSignInView;
    private View mCreateAccountView;
    private ProgressBar mProgressBar;
    private AuthorizationManager mAuthorizationManager;
    private Button mOKButton;
    private AuthButtonsController btnsController;
    private LinearLayout mOtherSocialNetworksButton;
    private static final String MAIN_BUTTONS_GA_TAG = "LoginButtonsTest";
    private boolean additionalButtonsScreen = false;
    private boolean btnsHidden;

    private boolean mButtonsInitialized = false;
    private ImageView mVkIcon;
    private ImageView mOkIcon;
    private ImageView mFbIcon;
    private boolean mNeedShowButtonsOnResume = true;
    private BroadcastReceiver authorizationReceiver;
    private boolean authReceiverRegistered;

    public static AuthFragment newInstance() {
        return new AuthFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        Debug.log("AF: onCreate");

        View root = inflater.inflate(R.layout.fragment_auth, null);
        if (savedInstanceState != null) {
            btnsHidden = savedInstanceState.getBoolean(BTNS_HIDDEN);
        }
        initViews(root);
        initAuthorizationHandler();
        //Если у нас нет токена
        if (!AuthToken.getInstance().isEmpty()) {
            //Если мы попали на этот фрагмент с работающей авторизацией, то просто перезапрашиваем профиль
            loadAllProfileData();
        }
        checkOnline();
        return root;
    }

    protected void initAuthorizationHandler() {
        if (authorizationReceiver == null || !authReceiverRegistered) {
            authorizationReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    int msg = intent.getIntExtra(MSG_AUTH_KEY, Authorizer.AUTHORIZATION_CANCELLED);
                    switch (msg) {
                        case Authorizer.AUTHORIZATION_FAILED:
                            authorizationFailed(ErrorCodes.NETWORK_CONNECT_ERROR, null);
                            break;
                        case Authorizer.DIALOG_COMPLETED:
                            hideButtons();
                            break;
                        case Authorizer.TOKEN_RECEIVED:
                            if (getActivity() != null) {
                                auth(AuthToken.getInstance());
                            }
                            break;
                        case Authorizer.AUTHORIZATION_CANCELLED:
                            showButtons();
                            break;
                    }
                }
            };
            authReceiverRegistered = true;
            LocalBroadcastManager.getInstance(getActivity()).registerReceiver(authorizationReceiver, new IntentFilter(Authorizer.AUTHORIZATION_TAG));
        }
    }

    @Override
    protected void initViews(final View root) {
        super.initViews(root);
        initButtons(root);

        btnsController = new AuthButtonsController(getActivity(), new AuthButtonsController.OnButtonsSettingsLoadedHandler() {
            @Override
            public void buttonSettingsLoaded(HashSet<String> settings) {
                if (btnsController != null) {
                    setAuthInterface();
                }
            }
        });

        initOtherViews(root);
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
                    EasyTracker.sendEvent(MAIN_BUTTONS_GA_TAG, "OtherWaysButtonClicked", btnsController.getLocaleTag(), 1L);
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
                    btnTFClick();
                }
            }
        });

        mCreateAccountView = root.findViewById(R.id.loCreateAccount);
        mCreateAccountView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EasyTracker.sendEvent("Registration", "StartActivity", "FromAuth", 1L);
                Intent intent = new Intent(getActivity(), RegistrationActivity.class);
                startActivityForResult(intent, RegistrationActivity.INTENT_REGISTRATION);
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mAuthorizationManager.onActivityResult(requestCode, resultCode, data);

        if (mAuthorizationManager != null) {
            hideButtons();
            mAuthorizationManager.onActivityResult(requestCode, resultCode, data);
        }
        if (resultCode == Activity.RESULT_OK &&
                (requestCode == PasswordRecoverActivity.INTENT_RECOVER_PASSWORD
                        || requestCode == RegistrationActivity.INTENT_REGISTRATION)) {
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

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(BTNS_HIDDEN, btnsHidden);
    }

    @Override
    protected void showButtons() {
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
            mCreateAccountView.setVisibility(View.VISIBLE);
            mProgressBar.setVisibility(View.GONE);
        }
    }

    @Override
    protected void hideButtons() {
        if (mButtonsInitialized && isAdded()) {
            btnsHidden = true;
            mFBButton.setVisibility(View.GONE);
            mVKButton.setVisibility(View.GONE);
            mOKButton.setVisibility(View.GONE);
            mOtherSocialNetworksButton.setVisibility(View.GONE);
            mSignInView.setVisibility(View.GONE);
            mCreateAccountView.setVisibility(View.GONE);
            hideRetrier();
            mProgressBar.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void showProgress() {
        mProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    protected void hideProgress() {
        mProgressBar.setVisibility(View.GONE);
    }


    private void btnVKClick() {
        EasyTracker.sendEvent(MAIN_BUTTONS_GA_TAG, additionalButtonsScreen ? "LoginAdditionalVk" : "LoginMainVk", btnsController.getLocaleTag(), 1L);

        if (checkOnline() && mAuthorizationManager != null) {
            hideButtons();
            mAuthorizationManager.vkontakteAuth();
        }
    }

    private void btnFBClick() {
        EasyTracker.sendEvent(MAIN_BUTTONS_GA_TAG, additionalButtonsScreen ? "LoginAdditionalFb" : "LoginMainFb", btnsController.getLocaleTag(), 1L);
        if (checkOnline() && mAuthorizationManager != null) {
            hideButtons();
            mAuthorizationManager.facebookAuth();
        }
    }


    private void btnOKClick() {
        EasyTracker.sendEvent(MAIN_BUTTONS_GA_TAG, additionalButtonsScreen ? "LoginAdditionalOk" : "LoginMainOk", btnsController.getLocaleTag(), 1L);
        if (checkOnline() && mAuthorizationManager != null) {
            mAuthorizationManager.odnoklassnikiAuth(new Authorizer.OnTokenReceivedListener() {
                @Override
                public void onTokenReceived() {
                    mNeedShowButtonsOnResume = false;
                    hideButtons();
                }

                @Override
                public void onTokenReceiveFailed() {
                    mNeedShowButtonsOnResume = true;
                    showButtons();
                }
            });
        }
    }

    private void btnTFClick() {
        if (checkOnline() && mAuthorizationManager != null) {
            mAuthorizationManager.topfaceAuth();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuthorizationManager = AuthorizationManager.getInstance(getActivity());
        mAuthorizationManager.onCreate(savedInstanceState);
    }

    @Override
    protected void onSuccessAuthorization(AuthToken token) {
        if (!token.getSocialNet().equals(AuthToken.SN_TOPFACE)) {
            btnsController.addSocialNetwork(token.getSocialNet());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mAuthorizationManager.onResume();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        if (Ssid.isLoaded() && !AuthToken.getInstance().isEmpty()) {
            loadAllProfileData();
        } else if (mNeedShowButtonsOnResume) {
            showButtons();

        } else {
            mNeedShowButtonsOnResume = true;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        authReceiverRegistered = false;
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(authorizationReceiver);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mAuthorizationManager.onDestroy();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.show();
        }
        if (!hasAuthorized()) {
            EasyTracker.sendEvent(MAIN_BUTTONS_GA_TAG, additionalButtonsScreen ? "DismissAdditional" : "DismissMain", btnsController.getLocaleTag(), 1L);
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
