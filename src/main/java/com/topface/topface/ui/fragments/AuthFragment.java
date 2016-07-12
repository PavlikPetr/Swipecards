package com.topface.topface.ui.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.DataBindingUtil;
import android.databinding.ObservableFloat;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
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
import com.topface.topface.data.AuthTokenStateData;
import com.topface.topface.data.leftMenu.FragmentIdData;
import com.topface.topface.data.leftMenu.LeftMenuSettingsData;
import com.topface.topface.data.leftMenu.NavigationState;
import com.topface.topface.data.leftMenu.WrappedNavigationData;
import com.topface.topface.data.social.AppSocialAppsIds;
import com.topface.topface.databinding.FragmentAuthBinding;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.handlers.SimpleApiHandler;
import com.topface.topface.state.AuthState;
import com.topface.topface.ui.BaseFragmentActivity;
import com.topface.topface.ui.PasswordRecoverActivity;
import com.topface.topface.ui.RegistrationActivity;
import com.topface.topface.ui.RestoreAccountActivity;
import com.topface.topface.ui.TopfaceAuthActivity;
import com.topface.topface.utils.AuthServiceButtons;
import com.topface.topface.utils.AuthServiceButtons.SocServicesAuthButtons;
import com.topface.topface.utils.EasyTracker;
import com.topface.topface.utils.RxUtils;
import com.topface.topface.utils.Utils;
import com.topface.topface.utils.social.AuthToken;
import com.topface.topface.utils.social.AuthorizationManager;
import com.vk.sdk.dialogs.VKOpenAuthDialog;

import javax.inject.Inject;

import rx.Subscription;
import rx.functions.Action1;

public class AuthFragment extends BaseAuthFragment {

    public static final String TF_BUTTONS = "tf_buttons";
    public static final String REAUTH_INTENT = "com.topface.topface.action.AUTH";

    @Inject
    NavigationState mNavigationState;
    @Inject
    AuthState mAuthState;

    private Subscription mAuthStateSubscription;
    private static final String MAIN_BUTTONS_GA_TAG = "LoginButtonsTest";
    private static final String TRANSLATION_Y = "translationY";
    private static final String IMAGE_HTML_TEMPLATE = "<img src='%s'/> ";
    private static final int ANIMATION_PATH = 36;
    private static final long ANIMATION_DURATION = 500;
    private AuthorizationManager mAuthorizationManager;
    private boolean mIsSocNetBtnVisible = true;
    private boolean mIsTfBtnVisible = false;
    private Animation mButtonAnimation;
    private FragmentAuthBinding mBinding;
    private LoginFragmentHandler mLoginFragmentHandler;
    private boolean mGoToSocNetAuthScreen;
    private BroadcastReceiver mRestoreAccountShown = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mGoToSocNetAuthScreen = false;
            showButtons();
        }
    };
    private OnAuthButtonsClick mOnAuthButtonsClick = new OnAuthButtonsClick() {
        @Override
        public void onVkButtonClick() {
            EasyTracker.sendEvent(MAIN_BUTTONS_GA_TAG, "LoginMainVk", "", 1L);
            if (checkOnline() && mAuthorizationManager != null) {
                waitUntilAuthSocialOptions(new Runnable() {
                    @Override
                    public void run() {
                        mGoToSocNetAuthScreen = true;
                        mAuthorizationManager.vkontakteAuth(getActivity());
                    }
                });
            }
        }

        @Override
        public void onFbButtonClick() {
            EasyTracker.sendEvent(MAIN_BUTTONS_GA_TAG, "LoginMainFb", "", 1L);
            if (checkOnline() && mAuthorizationManager != null) {
                waitUntilAuthSocialOptions(new Runnable() {
                    @Override
                    public void run() {
                        mGoToSocNetAuthScreen = true;
                        mAuthorizationManager.facebookAuth(getActivity());
                    }
                });
            }
        }

        @Override
        public void onOkButtonClick() {
            EasyTracker.sendEvent(MAIN_BUTTONS_GA_TAG, "LoginMainOk", "", 1L);
            if (checkOnline() && mAuthorizationManager != null) {
                waitUntilAuthSocialOptions(new Runnable() {
                    @Override
                    public void run() {
                        mGoToSocNetAuthScreen = true;
                        mAuthorizationManager.odnoklassnikiAuth(getActivity());
                    }
                });
            }
        }

        @Override
        public void onTfButtonClick() {
            mGoToSocNetAuthScreen = false;
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

        @Override
        public void onEnteranceButtonClick() {
            if (getActivity() != null) {
                if (checkOnline() && mAuthorizationManager != null) {
                    mAuthorizationManager.topfaceAuth(getActivity());
                }
            }
        }

        @Override
        public void onUpButtonClick() {
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

        @Override
        public void onOtherSocButtonClick() {
            mGoToSocNetAuthScreen = false;
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

        @Override
        public void onCreateAccountButtonClick() {
            EasyTracker.sendEvent("Registration", "StartActivity", "FromAuth", 1L);
            startActivityForResult(new Intent(getContext(), RegistrationActivity.class), RegistrationActivity.INTENT_REGISTRATION);
        }
    };

    /**
     * @param visibility            - показать/скрыть кнопки авторизации через соц сети
     * @param isNeedChangeFlagState - запоминать состояние кнопок
     * @param isNeedAnimate         - анимировать появление
     */
    private void setAllSocNetBtnVisibility(boolean visibility, boolean isNeedChangeFlagState, boolean isNeedAnimate) {
        if (isAdded()) {
            if (isNeedChangeFlagState) {
                mIsSocNetBtnVisible = visibility;
            }
            // если на главном экране авторизации будет только одна кнопка соц. сервисов + авторизации через ТФ аккаунт (по умолчанию),
            // то отступ до кнопки ТФ уменьшаем, в противном случае отступ будет установлен в соответствии с плотностью экрана
            mLoginFragmentHandler.mTFButtonPaddingTop.set(getMainScreenServicesAvailable() > 1 ? getResources().getDimension(R.dimen.tf_auth_btn_top) : getResources().getDimension(R.dimen.auth_buttons_padding));
            mBinding.btnOtherServices.setVisibility(visibility && isOtherServicesButtonAvailable() ? View.VISIBLE : View.GONE);
            setVisibilityAndAnimateView(mBinding.btnAuthVK, visibility
                    && SocServicesAuthButtons.VK_BUTTON.isMainScreenLoginEnable()
                    && SocServicesAuthButtons.VK_BUTTON.isEnabled(), isNeedAnimate);
            setVisibilityAndAnimateView(mBinding.btnAuthFB, visibility
                    && SocServicesAuthButtons.FB_BUTTON.isMainScreenLoginEnable()
                    && SocServicesAuthButtons.FB_BUTTON.isEnabled(), isNeedAnimate);
            setVisibilityAndAnimateView(mBinding.btnAuthOk, visibility
                    && SocServicesAuthButtons.OK_BUTTON.isMainScreenLoginEnable()
                    && SocServicesAuthButtons.OK_BUTTON.isEnabled(), isNeedAnimate);
            setVisibilityAndAnimateView(mBinding.btnTfAccount, visibility
                    && SocServicesAuthButtons.TF_BUTTON.isEnabled(), isNeedAnimate);
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
                mIsTfBtnVisible = visibility;
            }
            if (visibility) {
                mBinding.btnOtherServices.setVisibility(View.GONE);
            }
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
        mLoginFragmentHandler = new LoginFragmentHandler(getContext());
        mLoginFragmentHandler.setOnAuthButtonsClickListener(mOnAuthButtonsClick);
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
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mAuthStateSubscription = mAuthState.getObservable(AuthTokenStateData.class)
                .subscribe(new Action1<AuthTokenStateData>() {
                    @Override
                    public void call(AuthTokenStateData authTokenStateData) {
                        switch (authTokenStateData.getStatus()) {
                            case AuthTokenStateData.TOKEN_READY:
                                auth(AuthToken.getInstance());
                                break;
                            case AuthTokenStateData.TOKEN_FAILED:
                                Utils.showToastNotification(R.string.general_reconnect_social, Toast.LENGTH_SHORT);
                            case AuthTokenStateData.TOKEN_PREPARING:
                                hideButtons();
                                showProgress();
                                break;
                            case AuthTokenStateData.TOKEN_AUTHORIZED:
                            case AuthTokenStateData.TOKEN_STATUS_UNDEFINED:
                            case AuthTokenStateData.TOKEN_NOT_READY:
                                hideProgress();
                                showButtons();
                                break;
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Debug.error("AuthStateData error " + throwable);
                    }
                });
    }

    @Override
    protected boolean isButterKnifeAvailable() {
        return false;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(TF_BUTTONS, mIsTfBtnVisible);
    }

    @Override
    protected void onOptionsAndProfileSuccess() {
        Activity activity = getActivity();
        if (isAdded() && activity instanceof BaseFragmentActivity) {
            ((BaseFragmentActivity) activity).close(this, true);
            mNavigationState.emmitNavigationState(new WrappedNavigationData(new LeftMenuSettingsData(FragmentIdData.DATING), WrappedNavigationData.SELECT_EXTERNALY));
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
            } else if (data != null && TextUtils.equals(data.getAction(), VKOpenAuthDialog.VK_RESULT_INTENT_NAME)) {
                hideProgress();
            }
        } else if (resultCode == Activity.RESULT_CANCELED) {
            hideProgress();
        }
    }

    @Override
    protected void showButtons() {
        if (mGoToSocNetAuthScreen) {
            return;
        }
        setAllSocNetBtnVisibility(mIsSocNetBtnVisible, false, false);
        setTfLoginBtnVisibility(mIsTfBtnVisible, false, false);
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
        LocalBroadcastManager.getInstance(App.getContext()).registerReceiver(mRestoreAccountShown,
                new IntentFilter(RestoreAccountActivity.RESTORE_ACCOUNT_SHOWN));
        Activity activity = getActivity();
        mButtonAnimation = AnimationUtils.loadAnimation(activity,
                R.anim.auth_button_anim);
        mAuthorizationManager = new AuthorizationManager();
        mAuthorizationManager.onCreate(savedInstanceState);
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
        LocalBroadcastManager.getInstance(App.getContext()).unregisterReceiver(mRestoreAccountShown);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Debug.log("AuthStateData unsubscribe");
        RxUtils.safeUnsubscribe(mAuthStateSubscription);
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
        return getMainScreenServicesAvailable() < getAllOtherServicesAvailableButtonsCount();
    }

    private int getMainScreenServicesAvailable() {
        int buttonsCount = 0;
        for (SocServicesAuthButtons keys : AuthServiceButtons.getOtherButtonsList().keySet()) {
            buttonsCount = keys.isMainScreenLoginEnable() && keys.isEnabled() ? buttonsCount + 1 : buttonsCount;
        }
        return buttonsCount;
    }

    private int getAllOtherServicesAvailableButtonsCount() {
        int buttonsCount = 0;
        for (SocServicesAuthButtons button : AuthServiceButtons.getOtherButtonsList().keySet()) {
            buttonsCount = button.isEnabled() ? buttonsCount + 1 : buttonsCount;
        }
        return buttonsCount;
    }

    @SuppressWarnings("unused")
    public static class LoginFragmentHandler {

        public final ObservableFloat mTFButtonPaddingTop = new ObservableFloat();

        private OnAuthButtonsClick mOnAuthButtonsClick;
        private Context mContext;

        public LoginFragmentHandler(Context context) {
            mContext = context;
        }

        public void setOnAuthButtonsClickListener(OnAuthButtonsClick listener) {
            mOnAuthButtonsClick = listener;
        }

        public void vkButtonClick(View view) {
            if (mOnAuthButtonsClick != null) {
                mOnAuthButtonsClick.onVkButtonClick();
            }
        }

        public void okButtonClick(View view) {
            if (mOnAuthButtonsClick != null) {
                mOnAuthButtonsClick.onOkButtonClick();
            }
        }

        public void fbButtonClick(View view) {
            if (mOnAuthButtonsClick != null) {
                mOnAuthButtonsClick.onFbButtonClick();
            }
        }

        public void tfButtonClick(View view) {
            if (mOnAuthButtonsClick != null) {
                mOnAuthButtonsClick.onTfButtonClick();
            }
        }

        public void entranceButtonClick(View view) {
            if (mOnAuthButtonsClick != null) {
                mOnAuthButtonsClick.onEnteranceButtonClick();
            }
        }

        public void createAccountButtonClick(View view) {
            if (mOnAuthButtonsClick != null) {
                mOnAuthButtonsClick.onCreateAccountButtonClick();
            }
        }

        public void upButtonClick(View view) {
            if (mOnAuthButtonsClick != null) {
                mOnAuthButtonsClick.onUpButtonClick();
            }
        }

        public void otherSocButtonClick(View view) {
            if (mOnAuthButtonsClick != null) {
                mOnAuthButtonsClick.onOtherSocButtonClick();
            }
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
            if (mContext == null) {
                return Utils.EMPTY;
            }
            String resString = mContext.getString(R.string.other_auth);
            for (SocServicesAuthButtons keys : AuthServiceButtons.getOtherButtonsList().keySet()) {
                resString = (!keys.isMainScreenLoginEnable() && keys.isEnabled() ? String.format(IMAGE_HTML_TEMPLATE, keys.name()) : Utils.EMPTY).concat(resString);
            }
            return resString;
        }

        private BitmapDrawable getResourceDrawable(int res) {
            BitmapDrawable d = new BitmapDrawable(mContext.getResources(), BitmapFactory.decodeResource(mContext.getResources(), res));
            d.setBounds(new Rect(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight() + d.getIntrinsicHeight() / 4));
            d.setGravity(Gravity.CENTER);
            return d;
        }
    }

    private interface OnAuthButtonsClick {
        void onVkButtonClick();

        void onFbButtonClick();

        void onOkButtonClick();

        void onTfButtonClick();

        void onEnteranceButtonClick();

        void onUpButtonClick();

        void onOtherSocButtonClick();

        void onCreateAccountButtonClick();
    }
}
