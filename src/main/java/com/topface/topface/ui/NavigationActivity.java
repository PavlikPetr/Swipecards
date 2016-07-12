package com.topface.topface.ui;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.BadParcelableException;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.appsflyer.AppsFlyerLib;
import com.topface.billing.OpenIabFragment;
import com.topface.framework.utils.Debug;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.data.City;
import com.topface.topface.data.CountersData;
import com.topface.topface.data.Options;
import com.topface.topface.data.Profile;
import com.topface.topface.data.leftMenu.DrawerLayoutStateData;
import com.topface.topface.data.leftMenu.FragmentIdData;
import com.topface.topface.data.leftMenu.LeftMenuSettingsData;
import com.topface.topface.data.leftMenu.NavigationState;
import com.topface.topface.data.leftMenu.WrappedNavigationData;
import com.topface.topface.promo.PromoPopupManager;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.SettingsRequest;
import com.topface.topface.requests.handlers.ApiHandler;
import com.topface.topface.state.DrawerLayoutState;
import com.topface.topface.state.EventBus;
import com.topface.topface.state.PopupHive;
import com.topface.topface.state.TopfaceAppState;
import com.topface.topface.ui.dialogs.AbstractDialogFragment;
import com.topface.topface.ui.dialogs.CitySearchPopup;
import com.topface.topface.ui.dialogs.DatingLockPopup;
import com.topface.topface.ui.dialogs.NotificationsDisablePopup;
import com.topface.topface.ui.dialogs.SetAgeDialog;
import com.topface.topface.ui.dialogs.TakePhotoPopup;
import com.topface.topface.ui.external_libs.adjust.AdjustAttributeData;
import com.topface.topface.ui.fragments.IOnBackPressed;
import com.topface.topface.ui.fragments.MenuFragment;
import com.topface.topface.ui.fragments.profile.OwnProfileFragment;
import com.topface.topface.ui.views.DrawerLayoutManager;
import com.topface.topface.ui.views.HackyDrawerLayout;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.CustomViewNotificationController;
import com.topface.topface.utils.IActionbarNotifier;
import com.topface.topface.utils.ISimpleCallback;
import com.topface.topface.utils.LocaleConfig;
import com.topface.topface.utils.NavigationManager;
import com.topface.topface.utils.PopupManager;
import com.topface.topface.utils.Utils;
import com.topface.topface.utils.ads.AdmobInterstitialUtils;
import com.topface.topface.utils.ads.FullscreenController;
import com.topface.topface.utils.config.UserConfig;
import com.topface.topface.utils.controllers.ChosenStartAction;
import com.topface.topface.utils.controllers.DatingInstantMessageController;
import com.topface.topface.utils.controllers.startactions.DatingLockPopupAction;
import com.topface.topface.utils.controllers.startactions.ExpressMessageAction;
import com.topface.topface.utils.controllers.startactions.IStartAction;
import com.topface.topface.utils.controllers.startactions.InvitePopupAction;
import com.topface.topface.utils.controllers.startactions.OnNextActionListener;
import com.topface.topface.utils.controllers.startactions.TrialVipPopupAction;
import com.topface.topface.utils.debug.FuckingVoodooMagic;
import com.topface.topface.utils.gcmutils.GCMUtils;
import com.topface.topface.utils.social.AuthToken;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;

import rx.functions.Action1;
import rx.functions.Func1;
import rx.subscriptions.CompositeSubscription;

import static com.topface.topface.state.PopupHive.AC_PRIORITY_HIGH;

public class NavigationActivity extends ParentNavigationActivity implements INavigationFragmentsListener {
    public static final String INTENT_EXIT = "com.topface.topface.is_user_banned";
    private static final String PAGE_SWITCH = "Page switch: ";
    public static final String FRAGMENT_SETTINGS = "fragment_settings";
    private static final int EXIT_TIMEOUT = 3000;

    private Intent mPendingNextIntent;
    private boolean mIsActionBarHidden;
    private View mContentFrame;
    private DrawerLayoutManager<HackyDrawerLayout> mDrawerLayout;
    private FullscreenController mFullscreenController;
    private boolean mIsPopupVisible = false;
    private boolean mActionBarOverlayed = false;
    private int mInitialTopMargin = 0;
    @SuppressWarnings("deprecation")
    private IActionbarNotifier mNotificationController;
    @Inject
    TopfaceAppState mAppState;
    @Inject
    PopupHive mPopupHive;
    @Inject
    NavigationState mNavigationState;
    @Inject
    DrawerLayoutState mDrawerLayoutState;
    @Inject
    EventBus mEventBus;
    private AtomicBoolean mBackPressedOnce = new AtomicBoolean(false);
    public static boolean isPhotoAsked;
    private PopupManager mPopupManager;
    private CompositeSubscription mSubscription = new CompositeSubscription();
    private NavigationManager mNavigationManager;

    /**
     * Перезапускает NavigationActivity, нужно например при смене языка
     *
     * @param activity активити, которое принадлежит тому же таску, что и старый NavigationActivity
     */
    public static void restartNavigationActivity(Activity activity, Options options) {
        Intent intent = new Intent(activity, NavigationActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .putExtra(GCMUtils.NEXT_INTENT, new LeftMenuSettingsData(options.startPage));
        if (App.getUserConfig().getDatingMessage().equals(options
                .instantMessageFromSearch.getText())) {
            intent.putExtra(DatingInstantMessageController.DEFAULT_MESSAGE, true);
        }
        activity.startActivity(intent);
    }

    @Override
    protected void initActionBarOptions(ActionBar actionBar) {
        if (actionBar != null) {
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM,
                    ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_HOME_AS_UP
                            | ActionBar.DISPLAY_SHOW_TITLE
                            | ActionBar.DISPLAY_SHOW_HOME
                            | ActionBar.DISPLAY_HOME_AS_UP);
            mNotificationController = new CustomViewNotificationController(actionBar);
        }
    }

    @Override
    protected void setActionBarView() {
        actionBarView.setLeftMenuView();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            UserConfig config = App.getUserConfig();
            config.setStartPositionOfActions(0);
            config.saveConfig();
        }
        App.from(getApplicationContext()).inject(this);
        Intent intent = getIntent();
        try {
            if (intent.getBooleanExtra(INTENT_EXIT, false)) {
                finish();
            }
        } catch (BadParcelableException e) {
            Debug.error(e);
        }
        setNeedTransitionAnimation(false);
        super.onCreate(savedInstanceState);
        mSubscription.add(mAppState.getObservable(CountersData.class).subscribe(new Action1<CountersData>() {
            @Override
            public void call(CountersData countersData) {
                if (mNotificationController != null) {
                    mNotificationController.refreshNotificator(countersData.getDialogs(), countersData.getMutual());
                }
            }
        }));
        mSubscription.add(mAppState.getObservable(AdjustAttributeData.class).subscribe(new Action1<AdjustAttributeData>() {
            @Override
            public void call(AdjustAttributeData adjustAttributionData) {
                App.sendReferreRequest(adjustAttributionData);
            }
        }));
        mSubscription.add(mNavigationState.getNavigationObservable().filter(new Func1<WrappedNavigationData, Boolean>() {
            @Override
            public Boolean call(WrappedNavigationData wrappedNavigationData) {
                return wrappedNavigationData != null && wrappedNavigationData.getStatesStack().contains(WrappedNavigationData.FRAGMENT_SWITCHED);
            }
        }).subscribe(new Action1<WrappedNavigationData>() {
            @Override
            public void call(WrappedNavigationData wrappedLeftMenuSettingsData) {
                if (wrappedLeftMenuSettingsData != null) {
                    if (wrappedLeftMenuSettingsData.getData().isOverlayed()) {
                        switchContentTopMargin(true);
                    } else if (mActionBarOverlayed) {
                        switchContentTopMargin(false);
                    }
                }
                mDrawerLayout.close();
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                throwable.printStackTrace();
            }
        }));
        mSubscription.add(mDrawerLayoutState.getObservable().subscribe(new Action1<DrawerLayoutStateData>() {
            @Override
            public void call(DrawerLayoutStateData drawerLayoutStateData) {
                switch (drawerLayoutStateData.getState()) {
                    case DrawerLayoutStateData.STATE_CHANGED:
                        if (mDrawerLayout != null && mDrawerLayout.getDrawer() != null) {
                            Utils.hideSoftKeyboard(NavigationActivity.this, mDrawerLayout.getDrawer().getWindowToken());
                        }
                        break;
                }
            }
        }));
        mSubscription.add(mEventBus.getObservable(City.class).subscribe(new Action1<City>() {
            @Override
            public void call(final City city) {
                if (city != null) {
                    SettingsRequest request = new SettingsRequest(App.getContext());
                    request.cityid = city.id;
                    request.callback(new ApiHandler() {
                        @Override
                        public void success(IApiResponse response) {
                            Profile profile = App.get().getProfile();
                            profile.city = city;
                            mAppState.setData(profile);
                        }

                        @Override
                        public void fail(int codeError, IApiResponse response) {
                        }
                    }).exec();
                }
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                Debug.error("City change observable failed", throwable);
            }
        }));
        if (isNeedBroughtToFront(intent)) {
            // При открытии активити из лаунчера перезапускаем ее
            finish();
            return;
        }
        mContentFrame = findViewById(R.id.fragment_content);
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) mContentFrame.getLayoutParams();
        if (params != null) {
            mInitialTopMargin = params.topMargin;
        }
        if (intent.hasExtra(GCMUtils.NEXT_INTENT)) {
            mPendingNextIntent = intent;
        }
        initNavigationManager(savedInstanceState);
        initDrawerLayout();
        initFullscreen();
        initAppsFlyer();
        isPhotoAsked = false;
        mSubscription.add(mAppState.getObservable(City.class).subscribe(new Action1<City>() {
            @Override
            public void call(final City city) {
                if (city != null) {
                    SettingsRequest request = new SettingsRequest(App.getContext());
                    request.cityid = city.id;
                    request.callback(new ApiHandler() {
                        @Override
                        public void success(IApiResponse response) {
                            App.from(NavigationActivity.this).getProfile().city = city;
                            CacheProfile.sendUpdateProfileBroadcast();
                        }

                        @Override
                        public void fail(int codeError, IApiResponse response) {
                        }
                    }).exec();
                }
            }
        }));
        Debug.log("PopupHive onCreate");
        if (savedInstanceState != null) {
            startPopupRush(true, true);
        } else {
            registerPopupSequence();
        }

    }

    @NotNull
    private List<IStartAction> getActionsList() {
        List<IStartAction> startActions = new ArrayList<>();
        mPopupManager = new PopupManager(this);
        startActions.add(new ExpressMessageAction(this, AC_PRIORITY_HIGH));
        startActions.add(new ChosenStartAction().chooseFrom(
                new TrialVipPopupAction(this, AC_PRIORITY_HIGH),
                new DatingLockPopupAction(getSupportFragmentManager(), AC_PRIORITY_HIGH,
                        new DatingLockPopup.DatingLockPopupRedirectListener() {
                            @Override
                            public void onRedirect() {
                                showFragment(new LeftMenuSettingsData(FragmentIdData.TABBED_LIKES));
                            }
                        })
        ));
        if (mFullscreenController != null) {
            startActions.add(mFullscreenController.createFullscreenStartAction(PopupHive.AC_PRIORITY_NORMAL, this));
        }
        startActions.add(new ChosenStartAction().chooseFrom(selectPhotoStartAction(AC_PRIORITY_HIGH),
                chooseCityStartAction(AC_PRIORITY_HIGH)));
        startActions.add(new NotificationsDisablePopup(NavigationActivity.this, AC_PRIORITY_HIGH));
        startActions.add(new PromoPopupManager(this).createPromoPopupStartAction(PopupHive.AC_PRIORITY_NORMAL));
        startActions.add(new InvitePopupAction(this, AC_PRIORITY_HIGH));
        startActions.add(mPopupManager.createRatePopupStartAction(PopupHive.AC_PRIORITY_NORMAL, App.get().getOptions().ratePopupTimeout, App.get().getOptions().ratePopupEnabled));
        startActions.add(mPopupManager.createOldVersionPopupStartAction(AC_PRIORITY_HIGH));
        return startActions;
    }

    private void registerPopupSequence() {
        if (!mPopupHive.containSequence(NavigationActivity.class)) {
            mPopupHive.registerPopupSequence(getActionsList(), NavigationActivity.class, false);
        }
    }

    private void startPopupRush(boolean isNeedResetOldSequence, boolean stateChanged) {
        if (!App.get().getProfile().isFromCache
                && App.get().isUserOptionsObtainedFromServer()
                && !CacheProfile.isEmpty() && !AuthToken.getInstance().isEmpty()) {
            if (stateChanged) {
                registerPopupSequence();
            }
            mPopupHive.execPopupRush(NavigationActivity.class, isNeedResetOldSequence);
        }
    }

    @Override
    protected int getContentLayoutId() {
        return R.layout.ac_navigation;
    }

    private void initFullscreen() {
        mFullscreenController = new FullscreenController(this);
    }

    private void initBonusCounterConfig() {
        long lastTime = App.getUserConfig().getBonusCounterLastShowTime();
        CacheProfile.needShowBonusCounter = lastTime < App.from(this).getOptions().bonus.timestamp;
    }

    private NavigationManager getNavigationManager() {
        if (mNavigationManager == null) {
            mNavigationManager = initNavigationManager(null);
        }
        return mNavigationManager;
    }

    private NavigationManager initNavigationManager(Bundle savedInstanceState) {
        // use startPage settings from server like default
        LeftMenuSettingsData fragmentSettings = new LeftMenuSettingsData(App.get().getOptions().startPage);
        if (mPendingNextIntent != null) {
            // if pending intent not null, than it has fragmentSettings from notification
            fragmentSettings = mPendingNextIntent.getParcelableExtra(GCMUtils.NEXT_INTENT);
            mPendingNextIntent = null;
        } else if (savedInstanceState != null && savedInstanceState.containsKey(FRAGMENT_SETTINGS)) {
            // get fragmentSettings from bundle
            fragmentSettings = savedInstanceState.getParcelable(FRAGMENT_SETTINGS);
        }
        mNavigationManager = new NavigationManager(this, fragmentSettings);
        mNavigationManager.setNeedCloseMenuListener(new ISimpleCallback() {
            @Override
            public void onCall() {
                if (mDrawerLayout != null) {
                    mDrawerLayout.close();
                }
            }
        });
        return mNavigationManager;
    }

    private void initLeftMenu() {
        MenuFragment leftMenu = (MenuFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_menu);
        if (leftMenu == null) {
            leftMenu = new MenuFragment();
        }
        if (!leftMenu.isAdded()) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.fragment_menu, leftMenu)
                    .commit();
        }
    }

    @Override
    @FuckingVoodooMagic(description = "если ничего не сохранять в стейт, то перестанет показывться очередь(см. onCreate)")
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(FRAGMENT_SETTINGS, getNavigationManager().getCurrentFragmentSettings());
    }

    private void initDrawerLayout() {
        getNavigationManager().init(getSupportFragmentManager());
        initLeftMenu();
        mDrawerLayout = new DrawerLayoutManager<>((HackyDrawerLayout) findViewById(R.id.loNavigationDrawer));
        mDrawerLayout.initLeftMneuDrawerLayout();
    }

    private void initAppsFlyer() {
        try {
            AppsFlyerLib.setAppsFlyerKey(getString(R.string.appsflyer_dev_key));
            AppsFlyerLib.sendTracking(getApplicationContext());
        } catch (Exception e) {
            Debug.error("AppsFlayer exception: ", e);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showFragment(LeftMenuSettingsData fragmentSettings) {
        Debug.log(PAGE_SWITCH + "show fragment: " + fragmentSettings);
        getNavigationManager().selectFragment(fragmentSettings);
    }

    private void showFragment(Intent intent) {
        //Получаем id фрагмента, если он открыт
        LeftMenuSettingsData currentFragment = intent.getParcelableExtra(GCMUtils.NEXT_INTENT);
        Debug.log(PAGE_SWITCH + "show fragment from NEXT_INTENT: " + currentFragment);
        showFragment(currentFragment == null ? new LeftMenuSettingsData(App.from(this).getOptions().startPage) : currentFragment);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mFullscreenController != null) {
            mFullscreenController.onPause();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.hasExtra(GCMUtils.NEXT_INTENT)) {
            showFragment(intent);
        }
    }

    private void tryPostponedStartFragment() {
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(FRAGMENT_SETTINGS)) {
            LeftMenuSettingsData data = intent.getExtras().getParcelable(FRAGMENT_SETTINGS);
            if (data != null) {
                showFragment(data);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        tryPostponedStartFragment();
        if (mFullscreenController != null) {
            mFullscreenController.onResume();
            if (!mPopupHive.isSequencedExecuted(NavigationActivity.class) &&
                    mFullscreenController.canShowFullscreen() && !AuthToken.getInstance().isEmpty()) {
                mFullscreenController.requestFullscreen();
            }
        }
        //restart -> open NavigationActivity
        if (App.getLocaleConfig().fetchToSystemLocale()) {
            LocaleConfig.changeLocale(this, App.getLocaleConfig().getApplicationLocale());
            return;
        }
        LocaleConfig.localeChangeInitiated = false;
        App.checkProfileUpdate();
    }

    @Override
    protected void onOptionsUpdated() {
        startPopupRush(false, false);
    }

    @Override
    protected void onProfileUpdated() {
        super.onProfileUpdated();
        if (App.get().getProfile().age < App.getAppOptions().getUserAgeMin()) {
            SetAgeDialog.newInstance().show(getSupportFragmentManager(), SetAgeDialog.TAG);
        }
        startPopupRush(false, false);
        initBonusCounterConfig();
    }

    /**
     * Select city if profile is empty
     *
     * @return start action object to register
     */
    private IStartAction chooseCityStartAction(final int priority) {
        return new IStartAction() {

            private OnNextActionListener mChooseCityNextActionListener;

            @Override
            public void callInBackground() {
            }

            @Override
            public void callOnUi() {
                CitySearchPopup popup = (CitySearchPopup) getSupportFragmentManager().findFragmentByTag(CitySearchPopup.TAG);
                if (popup == null) {
                    popup = CitySearchPopup.getInstance();
                }
                popup.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        mChooseCityNextActionListener.onNextAction();
                    }
                });
                popup.show(getSupportFragmentManager(), CitySearchPopup.TAG);
            }

            @Override
            public boolean isApplicable() {
                Profile profile = App.get().getProfile();
                return profile.city == null || profile.city.isEmpty();
            }

            @Override
            public int getPriority() {
                return priority;
            }

            @Override
            public String getActionName() {
                return "SelectCity";
            }

            @Override
            public void setStartActionCallback(OnNextActionListener startActionCallback) {
                mChooseCityNextActionListener = startActionCallback;
            }
        };
    }

    /**
     * Take photo if profile is empty
     *
     * @return start action object to register
     */
    private IStartAction selectPhotoStartAction(final int priority) {
        return new IStartAction() {

            private OnNextActionListener mSelectPhotoNextActionListener;

            @Override
            public void callInBackground() {
            }

            @Override
            public void callOnUi() {
                if (!isPhotoAsked) {
                    isPhotoAsked = true;
                    TakePhotoPopup popup = (TakePhotoPopup) getSupportFragmentManager().findFragmentByTag(TakePhotoPopup.TAG);
                    if (popup == null) {
                        popup = TakePhotoPopup.newInstance("");
                    }
                    popup.setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            mSelectPhotoNextActionListener.onNextAction();
                        }
                    });
                    popup.show(getSupportFragmentManager(), TakePhotoPopup.TAG);
                }
            }

            @Override
            public boolean isApplicable() {
                return !AuthToken.getInstance().isEmpty() && (App.get().getProfile().photo == null)
                        && !App.getConfig().getUserConfig().isUserAvatarAvailable();
            }

            @Override
            public int getPriority() {
                return priority;
            }

            @Override
            public String getActionName() {
                return "TakePhoto";
            }

            @Override
            public void setStartActionCallback(OnNextActionListener startActionCallback) {
                mSelectPhotoNextActionListener = startActionCallback;
            }
        };
    }

    public void setPopupVisible(boolean visibility) {
        mIsPopupVisible = visibility;
    }

    @Override
    public void onBackPressed() {
        FragmentManager fm = getSupportFragmentManager();
        IOnBackPressed backPressedListener = null;
        for (Fragment fragment : fm.getFragments()) {
            if (fragment instanceof IOnBackPressed) {
                backPressedListener = (IOnBackPressed) fragment;
                break;
            }
        }
        if (backPressedListener == null || !backPressedListener.onBackPressed()) {
            if (getBackPressedListener() == null || !getBackPressedListener().onBackPressed()) {
                if (mFullscreenController != null && mFullscreenController.isFullScreenBannerVisible() && !mIsPopupVisible) {
                    mFullscreenController.hideFullscreenBanner((ViewGroup) findViewById(R.id.loBannerContainer));
                } else {
                    if (!mBackPressedOnce.get()) {
                        (new Timer()).schedule(new TimerTask() {
                            @Override
                            public void run() {
                                mBackPressedOnce.set(false);
                            }
                        }, EXIT_TIMEOUT);
                        mBackPressedOnce.set(true);
                        Utils.showToastNotification(R.string.press_back_more_to_close_app, Toast.LENGTH_SHORT);
                    } else {
                        finish();
                    }
                    mIsPopupVisible = false;
                }
            }
        }
    }

    public void setMenuLockMode(int lockMode) {
        setMenuLockMode(lockMode, null);
    }

    /**
     * Options for left menu DrawerLayout
     *
     * @param lockMode predefined lock modes DrawerLayout.LOCK_MODE_
     * @param listener additional listener for drawerLayout backPress
     */
    public void setMenuLockMode(int lockMode, HackyDrawerLayout.IBackPressedListener listener) {
        if (mDrawerLayout != null && mDrawerLayout.getDrawer() != null) {
            mDrawerLayout.getDrawer().setDrawerLockMode(lockMode, GravityCompat.START);
            mDrawerLayout.getDrawer().setBackPressedListener(listener);
        }
    }

    @Override
    protected void onLoadProfile() {
        super.onLoadProfile();
        Profile profile = App.from(this).getProfile();
        //Интеграция наших id юзера с AppsFlyer
        if (profile.uid > 0) {
            try {
                AppsFlyerLib.setAppUserId(Integer.toString(profile.uid));
            } catch (Exception e) {
                Debug.error(e);
            }
            Debug.log("Current User ID:" + profile.uid);
        }
        AdmobInterstitialUtils.preloadInterstitials(this, App.from(this).getOptions().interstitial);
    }

    @Override
    protected void onDestroy() {
        if (mFullscreenController != null) {
            mFullscreenController.onDestroy();
        }
        if (mNavigationManager != null) {
            mNavigationManager.onDestroy();
        }
        mPopupHive.releaseHive();
        if (mSubscription != null && !mSubscription.isUnsubscribed()) {
            mSubscription.unsubscribe();
        }
        if (mPopupManager != null) {
            mPopupManager.onDestroy();
        }
        super.onDestroy();
        AdmobInterstitialUtils.releaseInterstitials();
    }

    @Override
    public boolean isTrackable() {
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        AbstractDialogFragment currentPopup = mPopupManager != null ? mPopupManager.getCurrentDialog() : null;
        if (currentPopup != null) {
            currentPopup.onActivityResult(requestCode, resultCode, data);
        }
        //Хак для работы покупок, см подробнее в BillingFragment.processRequestCode()
        boolean isBillingRequestProcessed = OpenIabFragment.processRequestCode(
                getSupportFragmentManager(),
                requestCode,
                resultCode,
                data,
                OwnProfileFragment.class
        );
        if (resultCode == Activity.RESULT_OK && !isBillingRequestProcessed) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void toggleDrawerLayout() {
        if (!mIsActionBarHidden) {
            if (mDrawerLayout != null && mDrawerLayout.getDrawer() != null) {
                if (mDrawerLayout.getDrawer().isDrawerOpen(GravityCompat.START)) {
                    mDrawerLayout.getDrawer().closeDrawer(GravityCompat.START);
                } else {
                    mDrawerLayout.getDrawer().openDrawer(GravityCompat.START);
                }
            }
        }
    }

    @Override
    public boolean onKeyDown(int keycode, KeyEvent e) {
        switch (keycode) {
            case KeyEvent.KEYCODE_MENU:
                if (mDrawerLayout != null
                        && mDrawerLayout.getDrawer() != null
                        && mDrawerLayout.getDrawer().getDrawerLockMode(GravityCompat.START) ==
                        DrawerLayout.LOCK_MODE_UNLOCKED) {
                    toggleDrawerLayout();
                }
                return true;
        }
        return super.onKeyDown(keycode, e);
    }

    private void switchContentTopMargin(boolean actionbarOverlay) {
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) mContentFrame.getLayoutParams();
        if (params != null) {
            params.topMargin = actionbarOverlay ? 0 : mInitialTopMargin;
            mContentFrame.requestLayout();
            mActionBarOverlayed = actionbarOverlay;
        }
    }

    @Override
    public void onHideActionBar() {
        mIsActionBarHidden = true;
        setMenuLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
    }

    @Override
    public void onShowActionBar() {
        mIsActionBarHidden = false;
        setMenuLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        if (getSupportActionBar() != null) {
            getSupportActionBar().show();
        }
    }

    @Override
    public void onUpClick() {
        toggleDrawerLayout();
    }
}
