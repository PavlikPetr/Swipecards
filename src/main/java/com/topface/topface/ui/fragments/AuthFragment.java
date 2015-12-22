package com.topface.topface.ui.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
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

import com.facebook.FacebookSdk;
import com.topface.framework.utils.Debug;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.Ssid;
import com.topface.topface.data.social.AppSocialAppsIds;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.handlers.SimpleApiHandler;
import com.topface.topface.ui.BaseFragmentActivity;
import com.topface.topface.ui.PasswordRecoverActivity;
import com.topface.topface.ui.RegistrationActivity;
import com.topface.topface.ui.TopfaceAuthActivity;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.EasyTracker;
import com.topface.topface.utils.Utils;
import com.topface.topface.utils.social.AuthToken;
import com.topface.topface.utils.social.AuthorizationManager;
import com.topface.topface.utils.social.Authorizer;
import com.vk.sdk.dialogs.VKOpenAuthDialog;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AuthFragment extends BaseAuthFragment {

    public static final String TF_BUTTONS = "tf_buttons";
    public static final String REAUTH_INTENT = "com.topface.topface.action.AUTH";
    private static final String MAIN_BUTTONS_GA_TAG = "LoginButtonsTest";
    private static final String TRANSLATION_Y = "translationY";
    private static final int ANIMATION_PATH = 36;
    private static final long ANIMATION_DURATION = 500;
    private AuthorizationManager mAuthorizationManager;
    private boolean mIsSocNetBtnHidden = true;
    private boolean mIsTfBtnHidden = false;
    private Animation mButtonAnimation;

    @Bind(R.id.ivAuthGroup)
    View mAuthGroup;
    @Bind(R.id.prsAuthLoading)
    ProgressBar mProgressBar;
    @Bind(R.id.btnAuthFB)
    Button mFBButton;
    @Bind(R.id.btnAuthVK)
    Button mVKButton;
    @Bind(R.id.btnAuthOk)
    Button mOKButton;
    @Bind(R.id.btnTfAccount)
    Button mTfAccount;
    @Bind(R.id.btnEntrance)
    Button mSignIn;
    @Bind(R.id.btnCreateAccount)
    Button mCreateTfAccount;
    @Bind(R.id.tf_auth_back)
    ImageView mTfAuthBack;
    @Bind(R.id.ivAuthLogo)
    ImageView mTfLogo;

    @OnClick(R.id.btnAuthFB)
    public void btnFBClick() {
        EasyTracker.sendEvent(MAIN_BUTTONS_GA_TAG, "LoginMainFb", "", 1L);
        if (checkOnline() && mAuthorizationManager != null) {
            hideButtons();
            waitUntilAuthSocialOptions(new Runnable() {
                @Override
                public void run() {
                    mAuthorizationManager.facebookAuth(getActivity());
                }
            });

        }
    }

    @OnClick(R.id.btnAuthVK)
    public void btnVKClick() {
        EasyTracker.sendEvent(MAIN_BUTTONS_GA_TAG, "LoginMainVk", "", 1L);
        if (checkOnline() && mAuthorizationManager != null) {
            waitUntilAuthSocialOptions(new Runnable() {
                @Override
                public void run() {
                    mAuthorizationManager.vkontakteAuth(getActivity());
                }
            });
        }
    }

    @OnClick(R.id.btnAuthOk)
    public void btnOKClick() {
        EasyTracker.sendEvent(MAIN_BUTTONS_GA_TAG, "LoginMainOk", "", 1L);
        if (checkOnline() && mAuthorizationManager != null) {
            waitUntilAuthSocialOptions(new Runnable() {
                @Override
                public void run() {
                    mAuthorizationManager.odnoklassnikiAuth(getActivity());
                }
            });
        }
    }

    @OnClick(R.id.btnTfAccount)
    public void startTfAuthClick() {
        ObjectAnimator animator = ObjectAnimator.ofFloat(mAuthGroup, TRANSLATION_Y, 0, Utils.getPxFromDp(ANIMATION_PATH));
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                setSocNetBtnVisibility(false, true, false);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                setTfLoginBtnVisibility(true, true, true);
            }
        });
        animator.setDuration(ANIMATION_DURATION).start();
    }

    @OnClick(R.id.btnEntrance)
    public void signInClick() {
        if (getActivity() != null) {
            if (checkOnline() && mAuthorizationManager != null) {
                mAuthorizationManager.topfaceAuth(getActivity());
            }
        }
    }

    @OnClick(R.id.btnCreateAccount)
    public void createAccountClick() {
        EasyTracker.sendEvent("Registration", "StartActivity", "FromAuth", 1L);
        Intent intent = new Intent(getActivity(), RegistrationActivity.class);
        startActivityForResult(intent, RegistrationActivity.INTENT_REGISTRATION);
    }

    @OnClick(R.id.tf_auth_back)
    public void tfAuthBackClick() {
        ObjectAnimator animator = ObjectAnimator.ofFloat(mAuthGroup, TRANSLATION_Y, Utils.getPxFromDp(ANIMATION_PATH), 0);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                setTfLoginBtnVisibility(false, true, false);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                setSocNetBtnVisibility(true, true, true);
            }
        });
        animator.setDuration(ANIMATION_DURATION).start();
    }

    /**
     * @param visibility            - показать/скрыть кнопки авторизации через соц сети
     * @param isNeedChangeFlagState - запоминать состояние кнопок
     * @param isNeedAnimate         - анимировать появление
     */
    private void setSocNetBtnVisibility(boolean visibility, boolean isNeedChangeFlagState, boolean isNeedAnimate) {
        if (isAdded()) {
            if (isNeedChangeFlagState) {
                mIsSocNetBtnHidden = visibility;
            }
            setVisibilityAndAnimateView(mVKButton, visibility, isNeedAnimate);
            setVisibilityAndAnimateView(mFBButton, visibility, isNeedAnimate);
            setVisibilityAndAnimateView(mOKButton, visibility, isNeedAnimate);
            setVisibilityAndAnimateView(mTfAccount, visibility, isNeedAnimate);
        }
    }

    /**
     * @param visibility            - показать/скрыть кнопки авторизации через тф
     * @param isNeedChangeFlagState - запоминать состояние кнопок
     * @param isNeedAnimate         - анимировать появление
     */
    private void setTfLoginBtnVisibility(boolean visibility, boolean isNeedChangeFlagState, boolean isNeedAnimate) {
        if (isAdded()) {
            if (isNeedChangeFlagState) {
                mIsTfBtnHidden = visibility;
            }
            mTfAuthBack.setVisibility(visibility ? View.VISIBLE : View.GONE);
            setVisibilityAndAnimateView(mSignIn, visibility, isNeedAnimate);
            setVisibilityAndAnimateView(mCreateTfAccount, visibility, isNeedAnimate);
        }
    }

    private void setVisibilityAndAnimateView(View v, boolean visibility, boolean isNeedAnimate) {
        if (visibility && isNeedAnimate) {
            v.startAnimation(mButtonAnimation);
        } else {
            v.clearAnimation();
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
        return R.color.status_bar_color;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        Debug.log("AF: onCreate");
        View root = inflater.inflate(R.layout.fragment_auth, null);
        ButterKnife.bind(this, root);
        initViews(root);
        if (savedInstanceState != null && savedInstanceState.containsKey(TF_BUTTONS)) {
            setSocNetBtnVisibility(!savedInstanceState.getBoolean(TF_BUTTONS), true, false);
            setTfLoginBtnVisibility(savedInstanceState.getBoolean(TF_BUTTONS), true, false);
        }
        return root;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(TF_BUTTONS, mIsTfBtnHidden);
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
            } else if (TextUtils.equals(data.getAction(), VKOpenAuthDialog.VK_RESULT_INTENT_NAME)) {
                hideProgress();
            }
        } else if (resultCode == Activity.RESULT_CANCELED) {
            hideProgress();
            //show buttons back, if fb-login was canceled
            if(FacebookSdk.isFacebookRequestCode(requestCode)) {
                showButtons();
            }
        }
    }

    @Override
    protected void showButtons() {
        setSocNetBtnVisibility(mIsSocNetBtnHidden, false, false);
        setTfLoginBtnVisibility(mIsTfBtnHidden, false, false);
        mProgressBar.setVisibility(View.GONE);
    }

    @Override
    protected void hideButtons() {
        if (isAdded()) {
            setSocNetBtnVisibility(false, false, false);
            setTfLoginBtnVisibility(false, false, false);
            hideRetrier();
            mProgressBar.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void showProgress() {
        hideButtons();
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
        Activity activity = getActivity();
        mButtonAnimation = AnimationUtils.loadAnimation(activity,
                R.anim.auth_button_anim);
        mAuthorizationManager = new AuthorizationManager();
        mAuthorizationManager.onCreate(savedInstanceState);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mTokenReadyReceiver,
                new IntentFilter(Authorizer.AUTH_TOKEN_READY_ACTION));
    }

    @Override
    protected void onSuccessAuthorization(AuthToken token) {
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
            EasyTracker.sendEvent(MAIN_BUTTONS_GA_TAG, "DismissMain", "", 1L);
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
