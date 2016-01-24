package com.topface.topface.ui.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.databinding.DataBindingUtil;
import android.databinding.ObservableFloat;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.DimenRes;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import com.topface.framework.utils.Debug;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.Ssid;
import com.topface.topface.data.social.AppSocialAppsIds;
import com.topface.topface.databinding.FragmentAuthBinding;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.handlers.SimpleApiHandler;
import com.topface.topface.ui.BaseFragmentActivity;
import com.topface.topface.ui.PasswordRecoverActivity;
import com.topface.topface.ui.RegistrationActivity;
import com.topface.topface.ui.TopfaceAuthActivity;
import com.topface.topface.utils.AuthServiceButtons;
import com.topface.topface.utils.AuthServiceButtons.SocServicesAuthButtons;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.EasyTracker;
import com.topface.topface.utils.Utils;
import com.topface.topface.utils.social.AuthToken;
import com.topface.topface.utils.social.AuthorizationManager;
import com.topface.topface.utils.social.Authorizer;
import com.vk.sdk.dialogs.VKOpenAuthDialog;

public class AuthFragment extends BaseAuthFragment {

    public static final String TF_BUTTONS = "tf_buttons";
    public static final String REAUTH_INTENT = "com.topface.topface.action.AUTH";
    private static final String MAIN_BUTTONS_GA_TAG = "LoginButtonsTest";
    private static final String TRANSLATION_Y = "translationY";
    private static final String IMAGE_HTML_TEMPLATE = "<img src='%s'/> ";
    private static final int ANIMATION_PATH = 36;
    private static final long ANIMATION_DURATION = 500;
    private AuthorizationManager mAuthorizationManager;
    private boolean mIsSocNetBtnHidden = true;
    private boolean mIsTfBtnHidden = false;
    private Animation mButtonAnimation;
    private FragmentAuthBinding mBinding;
    private LoginFragmentHandler mLoginFragmentHandler;

    /**
     * @param visibility            - показать/скрыть кнопки авторизации через соц сети
     * @param isNeedChangeFlagState - запоминать состояние кнопок
     * @param isNeedAnimate         - анимировать появление
     */
    private void setAllSocNetBtnVisibility(boolean visibility, boolean isNeedChangeFlagState, boolean isNeedAnimate) {
        if (isAdded()) {
            if (isNeedChangeFlagState) {
                mIsSocNetBtnHidden = visibility;
            }
            // если на главном экране авторизации будет только одна кнопка соц. сервисов + авторизации через ТФ аккаунт (по умолчанию),
            // то отступ до кнопки ТФ уменьшаем, в противном случае отступ будет установлен в соответствии с плотностью экрана
            mLoginFragmentHandler.mTFButtonPaddingTop.set(getMainScreenServicesAvailable() > 1 ? getResources().getDimension(R.dimen.tf_auth_btn_top) : getResources().getDimension(R.dimen.auth_buttons_padding));
            mBinding.btnOtherServices.setVisibility(visibility && isOtherServicesButtonAvailable() ? View.VISIBLE : View.GONE);
            setVisibilityAndAnimateView(mBinding.btnAuthVK, visibility && SocServicesAuthButtons.VK_BUTTON.isMainScreenLoginEnable(), isNeedAnimate);
            setVisibilityAndAnimateView(mBinding.btnAuthFB, visibility && SocServicesAuthButtons.FB_BUTTON.isMainScreenLoginEnable(), isNeedAnimate);
            setVisibilityAndAnimateView(mBinding.btnAuthOk, visibility && SocServicesAuthButtons.OK_BUTTON.isMainScreenLoginEnable(), isNeedAnimate);
            setVisibilityAndAnimateView(mBinding.btnTfAccount, visibility, isNeedAnimate);
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
            mBinding.btnOtherServices.setVisibility(View.GONE);
            mBinding.tfAuthBack.setVisibility(visibility ? View.VISIBLE : View.GONE);
            setVisibilityAndAnimateView(mBinding.btnEntrance, visibility, isNeedAnimate);
            setVisibilityAndAnimateView(mBinding.btnCreateAccount, visibility, isNeedAnimate);
        }
    }

    /**
     * @param visibility    - показать/скрыть дополнительные кнопки авторизации
     * @param isNeedAnimate - анимировать появление
     */
    private void setExtraServicesBtnVisibility(boolean visibility, boolean isNeedAnimate) {
        if (isAdded()) {
            mBinding.tfAuthBack.setVisibility(visibility ? View.VISIBLE : View.GONE);
            mBinding.btnOtherServices.setVisibility(visibility ? View.GONE : isOtherServicesButtonAvailable() ? View.VISIBLE : View.GONE);
            setVisibilityAndAnimateView(mBinding.btnAuthFB, visibility && !SocServicesAuthButtons.FB_BUTTON.isMainScreenLoginEnable(), isNeedAnimate);
            setVisibilityAndAnimateView(mBinding.btnAuthOk, visibility && !SocServicesAuthButtons.OK_BUTTON.isMainScreenLoginEnable(), isNeedAnimate);
            setVisibilityAndAnimateView(mBinding.btnAuthVK, visibility && !SocServicesAuthButtons.VK_BUTTON.isMainScreenLoginEnable(), isNeedAnimate);
            // на экране других соц. сервисов кнопка авторизации в ТФ не нужна
            mBinding.btnTfAccount.setVisibility(View.GONE);
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
        mBinding = DataBindingUtil.bind(root);
        mLoginFragmentHandler = new LoginFragmentHandler();
        mBinding.setHandler(mLoginFragmentHandler);
        mBinding.btnOtherServices.setVisibility(isOtherServicesButtonAvailable() ? View.VISIBLE : View.GONE);
        initViews(root);
        if (savedInstanceState != null && savedInstanceState.containsKey(TF_BUTTONS)) {
            setAllSocNetBtnVisibility(!savedInstanceState.getBoolean(TF_BUTTONS), true, false);
            setTfLoginBtnVisibility(savedInstanceState.getBoolean(TF_BUTTONS), true, false);
        } else {
            setAllSocNetBtnVisibility(true, true, false);
        }
        return root;
    }

    @Override
    protected boolean isButterKnifeAvailable() {
        return false;
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
            MenuFragment.selectFragment(CacheProfile.getOptions().startPageFragmentSettings);
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
        }
    }

    @Override
    protected void showButtons() {
        setAllSocNetBtnVisibility(mIsSocNetBtnHidden, false, false);
        setTfLoginBtnVisibility(mIsTfBtnHidden, false, false);
        mBinding.prsAuthLoading.setVisibility(View.GONE);
    }

    @Override
    protected void hideButtons() {
        if (isAdded()) {
            setAllSocNetBtnVisibility(false, false, false);
            setTfLoginBtnVisibility(false, false, false);
            hideRetrier();
            mBinding.prsAuthLoading.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void showProgress() {
        hideButtons();
        mBinding.prsAuthLoading.setVisibility(View.VISIBLE);
    }

    @Override
    protected void hideProgress() {
        mBinding.prsAuthLoading.setVisibility(View.GONE);
    }

    @Override
    protected void showRetrier() {
        super.showRetrier();
        mBinding.authContainer.setVisibility(View.GONE);
        mBinding.prsAuthLoading.setVisibility(View.GONE);
    }

    @Override
    protected void hideRetrier() {
        super.hideRetrier();
        mBinding.authContainer.setVisibility(View.VISIBLE);
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

    private boolean isOtherServicesButtonAvailable() {
        return getMainScreenServicesAvailable() < SocServicesAuthButtons.values().length;
    }

    private int getMainScreenServicesAvailable() {
        int buttonsCount = 0;
        for (SocServicesAuthButtons item : SocServicesAuthButtons.values()) {
            buttonsCount = item.isMainScreenLoginEnable() ? buttonsCount + 1 : buttonsCount;
        }
        return buttonsCount;
    }

    public class LoginFragmentHandler {

        public final ObservableFloat mTFButtonPaddingTop = new ObservableFloat();

        public void VKButtonClick(View view) {
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

        public void OKButtonClick(View view) {
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

        public void FBButtonClick(View view) {
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

        public void TFButtonClick(View view) {
            ObjectAnimator animator = ObjectAnimator.ofFloat(mBinding.ivAuthGroup, TRANSLATION_Y, 0, Utils.getPxFromDp(ANIMATION_PATH));
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    setAllSocNetBtnVisibility(false, true, false);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    setTfLoginBtnVisibility(true, true, true);
                }
            });
            animator.setDuration(ANIMATION_DURATION).start();
        }

        public void EntranceButtonClick(View view) {
            if (getActivity() != null) {
                if (checkOnline() && mAuthorizationManager != null) {
                    mAuthorizationManager.topfaceAuth(getActivity());
                }
            }
        }

        public void CreateAccountButtonClick(View view) {
            EasyTracker.sendEvent("Registration", "StartActivity", "FromAuth", 1L);
            startActivityForResult(new Intent(getActivity(), RegistrationActivity.class), RegistrationActivity.INTENT_REGISTRATION);
        }

        public void UpButtonClick(View view) {
            ObjectAnimator animator = ObjectAnimator.ofFloat(mBinding.ivAuthGroup, TRANSLATION_Y, Utils.getPxFromDp(ANIMATION_PATH), 0);
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    setExtraServicesBtnVisibility(false, false);
                    setTfLoginBtnVisibility(false, true, false);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    setAllSocNetBtnVisibility(true, true, true);
                }
            });
            animator.setDuration(ANIMATION_DURATION).start();
        }

        public void OtherSocButtonClick(View view) {
            ObjectAnimator animator = ObjectAnimator.ofFloat(mBinding.ivAuthGroup, TRANSLATION_Y, 0, Utils.getPxFromDp(ANIMATION_PATH));
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    setAllSocNetBtnVisibility(false, true, false);
                    setTfLoginBtnVisibility(false, false, false);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    setExtraServicesBtnVisibility(true, true);
                }
            });
            animator.setDuration(ANIMATION_DURATION).start();
        }

        public Spanned getOtherServicesButtonText() {
            return Html.fromHtml(getOtherSocString(), new Html.ImageGetter() {
                @Override
                public Drawable getDrawable(String source) {
                    return getServiceIcon(source);
                }
            }, null);
        }

        private BitmapDrawable getServiceIcon(String name) {
            SocServicesAuthButtons button = null;
            try {
                button = SocServicesAuthButtons.valueOf(name);
            } catch (IllegalArgumentException e) {
                Debug.error("Illegal value of socButton", e);
            }
            return button != null ? getResourceDrawable(button.getSmallButtonsIconRes()) : null;
        }

        private String getOtherSocString() {
            String resString = getString(R.string.other_auth);
            for (SocServicesAuthButtons item : SocServicesAuthButtons.values()) {
                resString = (!item.isMainScreenLoginEnable() ? String.format(IMAGE_HTML_TEMPLATE, item.name()) : Utils.EMPTY).concat(resString);
            }
            return resString;
        }

        private BitmapDrawable getResourceDrawable(int res) {
            BitmapDrawable d = new BitmapDrawable(getResources(), BitmapFactory.decodeResource(getResources(), res));
            d.setBounds(new Rect(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight() + (int) getResources().getDimension(R.dimen.other_services_button_img_padding)));
            d.setGravity(Gravity.CENTER);
            return d;
        }
    }
}
