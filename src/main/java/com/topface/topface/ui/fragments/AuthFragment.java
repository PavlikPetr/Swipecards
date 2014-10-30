package com.topface.topface.ui.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
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
import com.topface.topface.ui.PasswordRecoverActivity;
import com.topface.topface.ui.RegistrationActivity;
import com.topface.topface.ui.TopfaceAuthActivity;
import com.topface.topface.utils.AuthButtonsController;
import com.topface.topface.utils.EasyTracker;
import com.topface.topface.utils.social.AuthToken;
import com.topface.topface.utils.social.AuthorizationManager;

import java.util.HashSet;

public class AuthFragment extends BaseAuthFragment {

    public static final String REAUTH_INTENT = "com.topface.topface.action.AUTH";
    public static final String BTNS_HIDDEN = "BtnsHidden";

    private View mLogo;
    private Button mFBButton;
    private Button mVKButton;
    private View mSignInView;
    private View mCreateAccountView;
    private ProgressBar mProgressBar;
    private AuthorizationManager mAuthorizationManager;
    private Button mOKButton;
    private AuthButtonsController mBtnsController;
    private LinearLayout mOtherSocialNetworksButton;
    private static final String MAIN_BUTTONS_GA_TAG = "LoginButtonsTest";
    private boolean mAdditionalButtonsScreen = false;
    private boolean mBtnsHidden;

    private boolean mButtonsInitialized = false;
    private ImageView mVkIcon;
    private ImageView mOkIcon;
    private ImageView mFbIcon;
    private boolean mNeedShowButtonsOnResume = true;

    public static AuthFragment newInstance() {
        return new AuthFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        Debug.log("AF: onCreate");

        View root = inflater.inflate(R.layout.fragment_auth, null);
        if (savedInstanceState != null) {
            mBtnsHidden = savedInstanceState.getBoolean(BTNS_HIDDEN);
        }
        initViews(root);
        //Если у нас нет токена
        if (!AuthToken.getInstance().isEmpty()) {
            //Если мы попали на этот фрагмент с работающей авторизацией, то просто перезапрашиваем профиль
            loadAllProfileData();
        }
        checkOnline();
        return root;
    }

    @Override
    protected void initViews(final View root) {
        super.initViews(root);
        initButtons(root);

        mBtnsController = new AuthButtonsController(getActivity(), new AuthButtonsController.OnButtonsSettingsLoadedHandler() {
            @Override
            public void buttonSettingsLoaded(HashSet<String> settings) {
                if (mBtnsController != null) {
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
                mAdditionalButtonsScreen = true;
                if (mBtnsController != null) {
                    mBtnsController.switchSettings();
                    EasyTracker.sendEvent(MAIN_BUTTONS_GA_TAG, "OtherWaysButtonClicked", mBtnsController.getLocaleTag(), 1L);
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
        if (mBtnsController == null || !isAdded()) return;
        if (mBtnsController.isSocialNetworkActive(AuthToken.SN_VKONTAKTE)) {
            mVKButton.setAnimation(AnimationUtils.loadAnimation(getActivity(),
                    R.anim.fade_in));
            mVKButton.setVisibility(View.VISIBLE);
        } else {
            mVKButton.setVisibility(View.GONE);
        }

        if (mBtnsController.isSocialNetworkActive(AuthToken.SN_FACEBOOK)) {
            mFBButton.setAnimation(AnimationUtils.loadAnimation(getActivity(),
                    R.anim.fade_in));
            mFBButton.setVisibility(View.VISIBLE);
        } else {
            mFBButton.setVisibility(View.GONE);
        }

        if (mBtnsController.isSocialNetworkActive(AuthToken.SN_ODNOKLASSNIKI)) {
            mOKButton.setAnimation(AnimationUtils.loadAnimation(getActivity(),
                    R.anim.fade_in));
            mOKButton.setVisibility(View.VISIBLE);
        } else {
            mOKButton.setVisibility(View.GONE);
        }

        HashSet<String> otherSN = mBtnsController.getOthers();
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

        if (mAuthorizationManager != null) {
            hideButtons();
            mAuthorizationManager.onActivityResult(requestCode, resultCode, data);
        }
        if (requestCode == TopfaceAuthActivity.INTENT_TOPFACE_AUTH && resultCode == Activity.RESULT_OK) {
            mNeedShowButtonsOnResume = false;
        } else if (resultCode == Activity.RESULT_OK &&
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
        } else if (resultCode == Activity.RESULT_OK) {
            AuthToken authToken = AuthToken.getInstance();
            if (!authToken.isEmpty()) {
                mNeedShowButtonsOnResume = false;
                auth(AuthToken.getInstance());
            }
        } else if (resultCode == Activity.RESULT_CANCELED) {
            hideProgress();
        }
    }

    private void initOtherViews(View root) {
        mLogo = root.findViewById(R.id.ivAuthLogo);
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
        outState.putBoolean(BTNS_HIDDEN, mBtnsHidden);
    }

    @Override
    protected void showButtons() {
        //Эта проверка нужна, для безопасной работы в
        mBtnsHidden = false;
        mLogo.setVisibility(View.VISIBLE);
        if (mFBButton != null && mVKButton != null && mProgressBar != null) {
            if (mBtnsController.isSocialNetworkActive(AuthToken.SN_FACEBOOK)) {
                mFBButton.setVisibility(View.VISIBLE);
            }
            if (mBtnsController.isSocialNetworkActive(AuthToken.SN_VKONTAKTE)) {
                mVKButton.setVisibility(View.VISIBLE);
            }
            if (mBtnsController.isSocialNetworkActive(AuthToken.SN_ODNOKLASSNIKI)) {
                mOKButton.setVisibility(View.VISIBLE);
            }
            if (mBtnsController.getOthers().size() > 0) {
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
            mBtnsHidden = true;
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
        hideButtons();
        mLogo.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    protected void hideProgress() {
        mProgressBar.setVisibility(View.GONE);
    }

    @Override
    protected void showRetrier() {
        super.showRetrier();
        mLogo.setVisibility(View.GONE);
    }

    private void btnVKClick() {
        EasyTracker.sendEvent(MAIN_BUTTONS_GA_TAG, mAdditionalButtonsScreen ? "LoginAdditionalVk" : "LoginMainVk", mBtnsController.getLocaleTag(), 1L);

        if (checkOnline() && mAuthorizationManager != null) {
            hideButtons();
            mAuthorizationManager.vkontakteAuth();
        }
    }

    private void btnFBClick() {
        EasyTracker.sendEvent(MAIN_BUTTONS_GA_TAG, mAdditionalButtonsScreen ? "LoginAdditionalFb" : "LoginMainFb", mBtnsController.getLocaleTag(), 1L);
        if (checkOnline() && mAuthorizationManager != null) {
            hideButtons();
            mAuthorizationManager.facebookAuth();
        }
    }


    private void btnOKClick() {
        EasyTracker.sendEvent(MAIN_BUTTONS_GA_TAG, mAdditionalButtonsScreen ? "LoginAdditionalOk" : "LoginMainOk", mBtnsController.getLocaleTag(), 1L);
        if (checkOnline() && mAuthorizationManager != null) {
            mAuthorizationManager.odnoklassnikiAuth();
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
        mAuthorizationManager = new AuthorizationManager(getActivity());
        mAuthorizationManager.onCreate(savedInstanceState);
    }

    @Override
    protected void onSuccessAuthorization(AuthToken token) {
        if (!token.getSocialNet().equals(AuthToken.SN_TOPFACE)) {
            mBtnsController.addSocialNetwork(token.getSocialNet());
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
    public void onDestroy() {
        super.onDestroy();
        mAuthorizationManager.onDestroy();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.show();
        }
        if (!hasAuthorized()) {
            EasyTracker.sendEvent(MAIN_BUTTONS_GA_TAG, mAdditionalButtonsScreen ? "DismissAdditional" : "DismissMain", mBtnsController.getLocaleTag(), 1L);
        }
    }

    @Override
    protected String getTitle() {
        return getString(R.string.app_name);
    }
}
