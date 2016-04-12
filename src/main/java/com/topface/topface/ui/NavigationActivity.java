package com.topface.topface.ui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
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
import com.topface.topface.data.FragmentSettings;
import com.topface.topface.data.Options;
import com.topface.topface.data.Profile;
import com.topface.topface.promo.PromoPopupManager;
import com.topface.topface.promo.dialogs.PromoExpressMessages;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.SettingsRequest;
import com.topface.topface.requests.handlers.ApiHandler;
import com.topface.topface.state.PopupHive;
import com.topface.topface.state.TopfaceAppState;
import com.topface.topface.ui.dialogs.AbstractDialogFragment;
import com.topface.topface.ui.dialogs.CitySearchPopup;
import com.topface.topface.ui.dialogs.DatingLockPopup;
import com.topface.topface.ui.dialogs.NotificationsDisablePopup;
import com.topface.topface.ui.dialogs.SetAgeDialog;
import com.topface.topface.ui.dialogs.TakePhotoPopup;
import com.topface.topface.ui.fragments.MenuFragment;
import com.topface.topface.ui.fragments.profile.OwnProfileFragment;
import com.topface.topface.ui.views.HackyDrawerLayout;
import com.topface.topface.utils.AddPhotoHelper;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.CustomViewNotificationController;
import com.topface.topface.utils.IActionbarNotifier;
import com.topface.topface.utils.LocaleConfig;
import com.topface.topface.utils.PopupManager;
import com.topface.topface.utils.Utils;
import com.topface.topface.utils.ads.AdmobInterstitialUtils;
import com.topface.topface.utils.ads.FullscreenController;
import com.topface.topface.utils.config.UserConfig;
import com.topface.topface.utils.controllers.ChosenStartAction;
import com.topface.topface.utils.controllers.DatingInstantMessageController;
import com.topface.topface.utils.controllers.startactions.DatingLockPopupAction;
import com.topface.topface.utils.controllers.startactions.IStartAction;
import com.topface.topface.utils.controllers.startactions.InvitePopupAction;
import com.topface.topface.utils.controllers.startactions.OnNextActionListener;
import com.topface.topface.utils.controllers.startactions.TrialVipPopupAction;
import com.topface.topface.utils.gcmutils.GCMUtils;
import com.topface.topface.utils.offerwalls.OfferwallsManager;
import com.topface.topface.utils.social.AuthToken;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;

import rx.Observable;
import rx.functions.Action1;
import rx.subjects.BehaviorSubject;
import rx.subscriptions.CompositeSubscription;

import static com.topface.topface.ui.fragments.BaseFragment.FragmentId;

public class NavigationActivity extends ParentNavigationActivity implements INavigationFragmentsListener {
    public static final String INTENT_EXIT = "EXIT";
    public static final String PAGE_SWITCH = "Page switch: ";

    public enum DRAWER_LAYOUT_STATE {
        STATE_CHANGED, SLIDE, OPENED, CLOSED
    }

    private Intent mPendingNextIntent;
    private boolean mIsActionBarHidden;
    private View mContentFrame;
    private MenuFragment mMenuFragment;
    private HackyDrawerLayout mDrawerLayout;
    private FullscreenController mFullscreenController;
    private boolean isPopupVisible = false;
    private boolean mActionBarOverlayed = false;
    private int mInitialTopMargin = 0;
    @SuppressWarnings("deprecation")
    private ActionBarDrawerToggle mDrawerToggle;
    private IActionbarNotifier mNotificationController;
    @Inject
    TopfaceAppState mAppState;
    @Inject
    PopupHive mPopupHive;
    private AtomicBoolean mBackPressedOnce = new AtomicBoolean(false);
    private AddPhotoHelper mAddPhotoHelper;
    public static boolean isPhotoAsked;
    private PopupManager mPopupManager;
    private BehaviorSubject<DRAWER_LAYOUT_STATE> mDrawerLayoutStateObservable;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            AddPhotoHelper.handlePhotoMessage(msg);
        }
    };
    private OnNextActionListener mSelectPhotoNextActionListener;
    private OnNextActionListener mChooseCityNextActionListener;

    private BroadcastReceiver mProfileUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (App.from(context).getProfile().age < App.getAppOptions().getUserAgeMin()) {
                SetAgeDialog.newInstance().show(getSupportFragmentManager(), SetAgeDialog.TAG);
            }
        }
    };
    private CompositeSubscription mNavigationsSubscriptions = new CompositeSubscription();

    /**
     * Перезапускает NavigationActivity, нужно например при смене языка
     *
     * @param activity активити, которое принадлежит тому же таску, что и старый NavigationActivity
     */
    public static void restartNavigationActivity(Activity activity, Options options) {
        Intent intent = new Intent(activity, NavigationActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .putExtra(GCMUtils.NEXT_INTENT, options.startPageFragmentSettings);
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
        if (intent.getBooleanExtra(INTENT_EXIT, false)) {
            finish();
        }
        setNeedTransitionAnimation(false);
        super.onCreate(savedInstanceState);
        mNavigationsSubscriptions.add(mAppState.getObservable(CountersData.class).subscribe(new Action1<CountersData>() {
            @Override
            public void call(CountersData countersData) {
                if (mNotificationController != null) {
                    mNotificationController.refreshNotificator(countersData.dialogs, countersData.mutual);
                }
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
        initDrawerLayout();
        initFullscreen();
        initAppsFlyer();
        if (intent.hasExtra(GCMUtils.NEXT_INTENT)) {
            mPendingNextIntent = intent;
        }
        isPhotoAsked = false;
        mNavigationsSubscriptions.add(mAppState.getObservable(City.class).subscribe(new Action1<City>() {
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
        startPopupRush(true, true);
    }

    @NotNull
    private List<IStartAction> getActionsList() {
        List<IStartAction> startActions = new ArrayList<>();
        mPopupManager = new PopupManager(this);
        startActions.add(chooseCityStartAction(PopupHive.AC_PRIORITY_HIGH));
        startActions.add(selectPhotoStartAction(PopupHive.AC_PRIORITY_HIGH));
        startActions.add(new NotificationsDisablePopup(NavigationActivity.this, PopupHive.AC_PRIORITY_HIGH));
        IStartAction fourthStageActions = new ChosenStartAction().chooseFrom(
                mPopupManager.createOldVersionPopupStartAction(PopupHive.AC_PRIORITY_HIGH),
                mPopupManager.createRatePopupStartAction(PopupHive.AC_PRIORITY_NORMAL, App.get().getOptions().ratePopupTimeout, App.get().getOptions().ratePopupEnabled)
        );
        startActions.add(fourthStageActions);
        IStartAction fifthStageActions = mFullscreenController != null ? new ChosenStartAction().chooseFrom(
                new TrialVipPopupAction(this, PopupHive.AC_PRIORITY_HIGH),
                mFullscreenController.createFullscreenStartAction(PopupHive.AC_PRIORITY_NORMAL, this)
        ) : new TrialVipPopupAction(this, PopupHive.AC_PRIORITY_HIGH);
        startActions.add(fifthStageActions);
        startActions.add(new DatingLockPopupAction(getSupportFragmentManager(), PopupHive.AC_PRIORITY_HIGH,
                new DatingLockPopup.DatingLockPopupRedirectListener() {
                    @Override
                    public void onRedirect() {
                        showFragment(FragmentId.TABBED_LIKES.getFragmentSettings());
                    }
                }, this));
        startActions.add(new InvitePopupAction(this, PopupHive.AC_PRIORITY_HIGH));
        PromoPopupManager promoPopupManager = new PromoPopupManager(this);
        IStartAction seventhStageActions = new ChosenStartAction().chooseFrom(
                PromoExpressMessages.createPromoPopupStartAction(PopupHive.AC_PRIORITY_HIGH, new PromoExpressMessages.PopupRedirectListener() {
                    @Override
                    public void onRedirect() {
                        showFragment(FragmentId.TABBED_DIALOGS.getFragmentSettings());
                        mDrawerLayoutStateObservable.onNext(DRAWER_LAYOUT_STATE.CLOSED);
                    }
                }),
                promoPopupManager.createPromoPopupStartAction(PopupHive.AC_PRIORITY_NORMAL)
        );
        startActions.add(seventhStageActions);
        startActions.add(new InvitePopupAction(this, PopupHive.AC_PRIORITY_HIGH));
        return startActions;
    }

    private void startPopupRush(boolean isNeedResetOldSequence, boolean stateChanged) {
        if (!App.get().getProfile().isFromCache
                && App.get().isUserOptionsObtainedFromServer()
                && !CacheProfile.isEmpty(this) && !AuthToken.getInstance().isEmpty()) {
            if (!mPopupHive.containSequence(NavigationActivity.class) || stateChanged) {
                mPopupHive.registerPopupSequence(getActionsList(), NavigationActivity.class);
            }
            mPopupHive.execPopupRush(NavigationActivity.class, isNeedResetOldSequence);
        }
    }

    @Override
    protected int getContentLayoutId() {
        return R.layout.ac_navigation;
    }

    private void initFullscreen() {
        mFullscreenController = new FullscreenController(this, App.get().getOptions());
    }

    private void initBonusCounterConfig() {
        long lastTime = App.getUserConfig().getBonusCounterLastShowTime();
        CacheProfile.needShowBonusCounter = lastTime < App.from(this).getOptions().bonus.timestamp;
    }

    @SuppressWarnings("deprecation")
    private void initDrawerLayout() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        mMenuFragment = (MenuFragment) fragmentManager.findFragmentById(R.id.fragment_menu);
        if (mMenuFragment == null) {
            mMenuFragment = new MenuFragment();
        }
        mMenuFragment.setOnFragmentSelected(new MenuFragment.OnFragmentSelectedListener() {
            @Override
            public void onFragmentSelected(FragmentSettings fragmentSettings) {
                mDrawerLayout.closeDrawer(GravityCompat.START);
            }
        });
        if (!mMenuFragment.isAdded()) {
            fragmentManager
                    .beginTransaction()
                    .add(R.id.fragment_menu, mMenuFragment)
                    .commit();
        }
        mDrawerLayoutStateObservable = BehaviorSubject.create();
        mDrawerLayout = (HackyDrawerLayout) findViewById(R.id.loNavigationDrawer);
        mDrawerLayout.setScrimColor(Color.argb(217, 0, 0, 0));
        mDrawerLayout.setDrawerShadow(R.drawable.shadow_left_menu_right, GravityCompat.START);
        mDrawerToggle = new ActionBarDrawerToggle(
                this, /* host Activity */
                mDrawerLayout, /* DrawerLayout object */
                Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2 ? android.R.color.transparent : R.drawable.empty_home_as_up,
                /* nav drawer icon to replace 'Up' caret */
                R.string.app_name, /* "open drawer" description */
                R.string.app_name /* "close drawer" description */
        ) {
            @Override
            public void onDrawerStateChanged(int newState) {
                super.onDrawerStateChanged(newState);
                Utils.hideSoftKeyboard(NavigationActivity.this, mDrawerLayout.getWindowToken());
                mDrawerLayoutStateObservable.onNext(DRAWER_LAYOUT_STATE.STATE_CHANGED);
            }

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);
                mDrawerLayoutStateObservable.onNext(DRAWER_LAYOUT_STATE.SLIDE);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                mDrawerLayoutStateObservable.onNext(DRAWER_LAYOUT_STATE.OPENED);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                mDrawerLayoutStateObservable.onNext(DRAWER_LAYOUT_STATE.CLOSED);
            }
        };
        mDrawerToggle.setDrawerIndicatorEnabled(false);
        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);
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
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (mDrawerToggle != null) mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (mDrawerToggle != null) mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerLayout.getDrawerLockMode(GravityCompat.START) == DrawerLayout.LOCK_MODE_UNLOCKED) {
            return mDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
        } else {
            switch (item.getItemId()) {
                case android.R.id.home:
                    return true;
                default:
                    return super.onOptionsItemSelected(item);
            }
        }
    }

    public void showFragment(FragmentSettings fragmentSettings) {
        Debug.log(PAGE_SWITCH + "show fragment: " + fragmentSettings);
        mMenuFragment.selectMenu(fragmentSettings);
    }

    private void showFragment(Intent intent) {
        //Получаем id фрагмента, если он открыт
        FragmentSettings currentFragment = intent.getParcelableExtra(GCMUtils.NEXT_INTENT);
        Debug.log(PAGE_SWITCH + "show fragment from NEXT_INTENT: " + currentFragment);
        showFragment(currentFragment == null ? App.from(this).getOptions().startPageFragmentSettings : currentFragment);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mFullscreenController != null) {
            mFullscreenController.onPause();
        }
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mProfileUpdateReceiver);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.hasExtra(GCMUtils.NEXT_INTENT)) {
            showFragment(intent);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mFullscreenController != null) {
            mFullscreenController.onResume();
        }
        //restart -> open NavigationActivity
        if (App.getLocaleConfig().fetchToSystemLocale()) {
            LocaleConfig.changeLocale(this, App.getLocaleConfig().getApplicationLocale());
            return;
        } else {
            LocaleConfig.localeChangeInitiated = false;
        }
        App.checkProfileUpdate();
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(mProfileUpdateReceiver, new IntentFilter(CacheProfile.PROFILE_UPDATE_ACTION));
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (isLoggedIn() && mPendingNextIntent != null) {
            showFragment(mPendingNextIntent);
            mPendingNextIntent = null;
        }
    }

    @Override
    protected void onOptionsUpdated() {
        startPopupRush(false, false);
    }

    @Override
    protected void onProfileUpdated() {
        startPopupRush(false, false);
        initBonusCounterConfig();
        // возможно что содержимое меню поменялось, надо обновить
        if (mMenuFragment != null) {
            mMenuFragment.updateAdapter();
        }
    }

    /**
     * Select city if profile is empty
     *
     * @return start action object to register
     */
    private IStartAction chooseCityStartAction(final int priority) {
        return new IStartAction() {

            @Override
            public void callInBackground() {
            }

            @Override
            public void callOnUi() {
                CitySearchPopup popup = new CitySearchPopup();
                popup.setRetainInstance(true);
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
                return CacheProfile.needToSelectCity(NavigationActivity.this);
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

            @Override
            public void callInBackground() {
            }

            @Override
            public void callOnUi() {
                if (!isPhotoAsked) {
                    isPhotoAsked = true;
                    TakePhotoPopup popup = TakePhotoPopup.newInstance("");
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
                return !AuthToken.getInstance().isEmpty() && (App.from(NavigationActivity.this).getProfile().photo == null)
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
        isPopupVisible = visibility;
    }

    @Override
    public void onBackPressed() {
        if (getBackPressedListener() == null || !getBackPressedListener().onBackPressed()) {
            if (mFullscreenController != null && mFullscreenController.isFullScreenBannerVisible() && !isPopupVisible) {
                mFullscreenController.hideFullscreenBanner((ViewGroup) findViewById(R.id.loBannerContainer));
            } else if (!mBackPressedOnce.get()) {
                (new Timer()).schedule(new TimerTask() {
                    @Override
                    public void run() {
                        mBackPressedOnce.set(false);
                    }
                }, 3000);
                mBackPressedOnce.set(true);
                Utils.showToastNotification(R.string.press_back_more_to_close_app, Toast.LENGTH_SHORT);
                isPopupVisible = false;
            } else {
                super.onBackPressed();
                isPopupVisible = false;
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
        if (mDrawerLayout != null) {
            mDrawerLayout.setDrawerLockMode(lockMode, GravityCompat.START);
            mDrawerLayout.setBackPressedListener(listener);
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
        if (mDrawerToggle != null) {
            mDrawerToggle.syncState();
        }
        /*
        Initialize Topface offerwall here to be able to start it quickly instead of PurchasesActivity
         */
        OfferwallsManager.initTfOfferwall(this, null);
        AdmobInterstitialUtils.preloadInterstitials(this, App.from(this).getOptions().interstitial);
    }

    @Override
    protected void onDestroy() {
        //Для запроса фото при следующем создании NavigationActivity
        if (mFullscreenController != null) {
            mFullscreenController.onDestroy();
        }
        mPopupHive.releaseHive();
        mDrawerToggle = null;
        if (mAddPhotoHelper != null) {
            mAddPhotoHelper.releaseHelper();
        }
        if (mNavigationsSubscriptions != null && !mNavigationsSubscriptions.isUnsubscribed()) {
            mNavigationsSubscriptions.unsubscribe();
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
        if (mAddPhotoHelper != null) {
            mAddPhotoHelper.processActivityResult(requestCode, resultCode, data);
        }
    }

    private void toggleDrawerLayout() {
        if (!mIsActionBarHidden) {
            if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                mDrawerLayout.closeDrawer(GravityCompat.START);
            } else {
                mDrawerLayout.openDrawer(GravityCompat.START);
            }
            mDrawerToggle.syncState();
        }
    }

    @Override
    public boolean onKeyDown(int keycode, KeyEvent e) {
        switch (keycode) {
            case KeyEvent.KEYCODE_MENU:
                if (mDrawerLayout.getDrawerLockMode(GravityCompat.START) ==
                        DrawerLayout.LOCK_MODE_UNLOCKED) {
                    toggleDrawerLayout();
                }
                return true;
        }
        return super.onKeyDown(keycode, e);
    }

    private AddPhotoHelper getAddPhotoHelper() {
        if (mAddPhotoHelper == null) {
            mAddPhotoHelper = new AddPhotoHelper(this);
            mAddPhotoHelper.setOnResultHandler(mHandler);
        }
        return mAddPhotoHelper;
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
    public void onFragmentSwitch(FragmentSettings fragmentSettings) {
        if (fragmentSettings.isOverlayed()) {
            switchContentTopMargin(true);
        } else if (mActionBarOverlayed) {
            switchContentTopMargin(false);
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

    public Observable<DRAWER_LAYOUT_STATE> getDrawerLayoutStateObservable() {
        return mDrawerLayoutStateObservable;
    }
}
