package com.topface.topface.ui.fragments;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.topface.framework.utils.Debug;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.Ssid;
import com.topface.topface.data.AppSocialAppsIds;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.handlers.SimpleApiHandler;
import com.topface.topface.ui.BaseFragmentActivity;
import com.topface.topface.ui.PasswordRecoverActivity;
import com.topface.topface.ui.RegistrationActivity;
import com.topface.topface.ui.TopfaceAuthActivity;
import com.topface.topface.utils.AuthButtonsController;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.EasyTracker;
import com.topface.topface.utils.Utils;
import com.topface.topface.utils.social.AuthToken;
import com.topface.topface.utils.social.AuthorizationManager;
import com.topface.topface.utils.social.Authorizer;
import com.vk.sdk.VKOpenAuthActivity;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AuthFragment extends BaseAuthFragment {

    public static final String REAUTH_INTENT = "com.topface.topface.action.AUTH";
    public static final String SOC_NET_BTNS_HIDDEN = "SocNetBtnsHidden";
    public static final String TF_BTNS_HIDDEN = "TfBtnsHidden";
    private static final String MAIN_BUTTONS_GA_TAG = "LoginButtonsTest";
    private AuthorizationManager mAuthorizationManager;
    private AuthButtonsController mBtnsController;

    private boolean mIsSocNetBtnHidden = true;
    private boolean mIsTfBtnHidden = false;
    private boolean mIsNeedAnimate;
    private Animation mButtonAnimation;

    @Bind(R.id.ivAuthGroup)
    View mAuthGroup;
    @Bind(R.id.ivAuthLogo)
    View mLogo;
    @Bind(R.id.prsAuthLoading)
    ProgressBar mProgressBar;

    @Bind(R.id.btnAuthFB)
    Button mFBButton;

    @OnClick(R.id.btnAuthFB)
    public void btnFBClick() {
        EasyTracker.sendEvent(MAIN_BUTTONS_GA_TAG, "LoginMainFb", mBtnsController.getLocaleTag(), 1L);
        if (checkOnline() && mAuthorizationManager != null) {
            hideButtons();
            waitUntilAuthSocialOptions(new Runnable() {
                @Override
                public void run() {
                    mAuthorizationManager.facebookAuth();
                }
            });

        }
    }

    @Bind(R.id.btnAuthVK)
    Button mVKButton;

    @OnClick(R.id.btnAuthVK)
    public void btnVKClick() {
        EasyTracker.sendEvent(MAIN_BUTTONS_GA_TAG, "LoginMainVk", mBtnsController.getLocaleTag(), 1L);

        if (checkOnline() && mAuthorizationManager != null) {
            hideButtons();
            waitUntilAuthSocialOptions(new Runnable() {
                @Override
                public void run() {
                    mAuthorizationManager.vkontakteAuth();
                }
            });
        }
    }

    @Bind(R.id.btnAuthOk)
    Button mOKButton;

    @OnClick(R.id.btnAuthOk)
    public void btnOKClick() {
        EasyTracker.sendEvent(MAIN_BUTTONS_GA_TAG, "LoginMainOk", mBtnsController.getLocaleTag(), 1L);
        if (checkOnline() && mAuthorizationManager != null) {
            waitUntilAuthSocialOptions(new Runnable() {
                @Override
                public void run() {
                    mAuthorizationManager.odnoklassnikiAuth();
                }
            });
        }
    }

    @Bind(R.id.btnTfAccount)
    Button mTfAccount;

    @OnClick(R.id.btnTfAccount)
    public void startTfAuthClick() {
        mTfAuthBack.setVisibility(View.VISIBLE);
        ObjectAnimator.ofFloat(mAuthGroup, "translationY", 0, castDpToPixel(36))
                .setDuration(500).start();
        mIsNeedAnimate = true;
        setSocNetBtnVisibility(false);
        setTfLoginBtnVisibility(true);
        mIsNeedAnimate = false;
    }

    @Bind(R.id.btnEntrance)
    Button mSignIn;

    @OnClick(R.id.btnEntrance)
    public void signInClick() {
        FragmentActivity activity = getActivity();
        if (activity != null) {
            if (checkOnline() && mAuthorizationManager != null) {
                mAuthorizationManager.topfaceAuth();
            }
        }
    }

    @Bind(R.id.btnCreateAccount)
    Button mCreateTfAccount;

    @OnClick(R.id.btnCreateAccount)
    public void createAccountClick() {
        EasyTracker.sendEvent("Registration", "StartActivity", "FromAuth", 1L);
        Intent intent = new Intent(getActivity(), RegistrationActivity.class);
        startActivityForResult(intent, RegistrationActivity.INTENT_REGISTRATION);
    }

    @Bind(R.id.tf_auth_back)
    ImageView mTfAuthBack;

    @OnClick(R.id.tf_auth_back)
    public void tfAuthBackClick() {
        ObjectAnimator.ofFloat(mAuthGroup, "translationY", castDpToPixel(36), 0)
                .setDuration(500).start();
        mTfAuthBack.setVisibility(View.GONE);
        mIsNeedAnimate = true;
        setSocNetBtnVisibility(true);
        setTfLoginBtnVisibility(false);
        mIsNeedAnimate = false;
    }

    private int castDpToPixel(int dp) {
        float scale = getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    private void setSocNetBtnVisibility(boolean visibility) {
        mIsSocNetBtnHidden = visibility;
        if (mBtnsController == null || !isAdded()) return;
        if (mBtnsController.isSocialNetworkActive(AuthToken.SN_VKONTAKTE)) {
            setVisibilityAndAnmateView(mVKButton, visibility);
        } else {
            setVisibilityAndAnmateView(mVKButton, false);
        }
        if (mBtnsController.isSocialNetworkActive(AuthToken.SN_FACEBOOK)) {
            setVisibilityAndAnmateView(mFBButton, visibility);
        } else {
            setVisibilityAndAnmateView(mFBButton, false);
        }
        if (mBtnsController.isSocialNetworkActive(AuthToken.SN_ODNOKLASSNIKI)) {
            setVisibilityAndAnmateView(mOKButton, visibility);
        } else {
            setVisibilityAndAnmateView(mOKButton, false);
        }
        setVisibilityAndAnmateView(mTfAccount, visibility);
    }

    private void setTfLoginBtnVisibility(boolean visibility) {
        if (isAdded()) {
            mIsTfBtnHidden = visibility;
            setVisibilityAndAnmateView(mSignIn, visibility);
            setVisibilityAndAnmateView(mCreateTfAccount, visibility);
        }
    }

    private void setVisibilityAndAnmateView(View v, boolean visibility) {
        if (visibility && mIsNeedAnimate) {
            v.startAnimation(mButtonAnimation);
        }
        v.setVisibility(visibility ? View.VISIBLE : View.GONE);
    }

    private BroadcastReceiver mTokenReadyReceiver = new BroadcastReceiver() {

        private int mLastStatusReceived = Integer.MIN_VALUE;

        @Override
        public void onReceive(Context context, Intent intent) {
            int tokenStatus = intent.getIntExtra(Authorizer.TOKEN_STATUS, Authorizer.TOKEN_NOT_READY);

            if (tokenStatus != mLastStatusReceived) {
                switch (tokenStatus) {
                    case Authorizer.TOKEN_READY:
                        auth(AuthToken.getInstance());
                        break;
                    case Authorizer.TOKEN_FAILED:
                        Utils.showToastNotification(R.string.general_reconnect_social, Toast.LENGTH_SHORT);
                    case Authorizer.TOKEN_NOT_READY:
                        hideProgress();
                        showButtons();
                        break;
                    case Authorizer.TOKEN_PREPARING:
                        hideButtons();
                        showProgress();
                        break;
                }
                mLastStatusReceived = tokenStatus;
            }
        }
    };

    public static AuthFragment newInstance() {
        return new AuthFragment();
    }

    @Override
    protected int getStatusBarColor() {
        return R.color.status_bar_dark_gray_color;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        Debug.log("AF: onCreate");
        View root = inflater.inflate(R.layout.fragment_auth, null);
        ButterKnife.bind(this, root);
        if (savedInstanceState != null) {
            setSocNetBtnVisibility(savedInstanceState.getBoolean(SOC_NET_BTNS_HIDDEN));
            setTfLoginBtnVisibility(savedInstanceState.getBoolean(TF_BTNS_HIDDEN));
        }
        initViews(root);

        return root;
    }

    @Override
    protected void initViews(final View root) {
        super.initViews(root);
        mBtnsController = new AuthButtonsController(getActivity());
        setSocNetBtnVisibility(true);
    }

    @Override
    protected void onOptionsAndProfileSuccess() {
        Activity activity = getActivity();
        if (isAdded() && activity instanceof BaseFragmentActivity) {
            ((BaseFragmentActivity) activity).close(this, true);
            MenuFragment.selectFragment(CacheProfile.getOptions().startPageFragmentId);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (mAuthorizationManager != null) {
            mAuthorizationManager.onActivityResult(requestCode, resultCode, data);
        }
        if (requestCode == TopfaceAuthActivity.INTENT_TOPFACE_AUTH && resultCode == Activity.RESULT_OK) {
            hideButtons();
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
            hideButtons();
            if (!authToken.isEmpty()) {
                auth(AuthToken.getInstance());
            } else if (TextUtils.equals(data.getAction(), VKOpenAuthActivity.VK_RESULT_INTENT_NAME)) {
                setTfLoginBtnVisibility(mIsSocNetBtnHidden);
                setTfLoginBtnVisibility(mIsTfBtnHidden);
                hideProgress();
            }
        } else if (resultCode == Activity.RESULT_CANCELED) {
            hideProgress();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(SOC_NET_BTNS_HIDDEN, mIsSocNetBtnHidden);
        outState.putBoolean(TF_BTNS_HIDDEN, mIsTfBtnHidden);
    }

    @Override
    protected void showButtons() {
        mLogo.setVisibility(View.VISIBLE);
        setSocNetBtnVisibility(mIsSocNetBtnHidden);
        setTfLoginBtnVisibility(mIsTfBtnHidden);
    }

    @Override
    protected void hideButtons() {
        if (isAdded()) {
            setSocNetBtnVisibility(false);
            setTfLoginBtnVisibility(false);
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
        mAuthGroup.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.GONE);
    }

    @Override
    protected void hideRetrier() {
        super.hideRetrier();
        mAuthGroup.setVisibility(View.VISIBLE);
    }

    private void waitUntilAuthSocialOptions(final Runnable runnable) {
        AppSocialAppsIds ids = App.getAppSocialAppsIds();
        if (ids == null) {
            App.from(getActivity()).createAppSocialAppsIdsRequest(new SimpleApiHandler() {
                @Override
                public void success(IApiResponse response) {
                    super.success(response);
                    runnable.run();
                }
            });
        } else {
            runnable.run();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mButtonAnimation = AnimationUtils.loadAnimation(getActivity(),
                R.anim.fade_in);
        mAuthorizationManager = new AuthorizationManager(getActivity());
        mAuthorizationManager.onCreate(savedInstanceState);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mTokenReadyReceiver,
                new IntentFilter(Authorizer.AUTH_TOKEN_READY_ACTION));
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
        setSocNetBtnVisibility(mIsSocNetBtnHidden);
        setTfLoginBtnVisibility(mIsTfBtnHidden);
        mAuthorizationManager.onResume();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        if (Ssid.isLoaded() && !AuthToken.getInstance().isEmpty()) {
            loadAllProfileData();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mAuthorizationManager.onPause();
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
            EasyTracker.sendEvent(MAIN_BUTTONS_GA_TAG, "DismissMain", mBtnsController.getLocaleTag(), 1L);
        }
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mTokenReadyReceiver);
    }

    @Override
    protected String getTitle() {
        /*
        * ВНИМАНИЕ - данное решение - хак
        * иначе, после выполнения здесь, в onDestroy(), actionBar.show();
        * в следующем фрагменте может обрезаться title, причем не сразу,
        * а только после подгрузки содержимого фида, например
        * */
        return getString(R.string.app_name) + "                          ";
    }
}
