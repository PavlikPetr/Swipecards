package com.topface.topface.ui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
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

import com.adjust.sdk.AdjustAttribution;
import com.appsflyer.AppsFlyerLib;
import com.topface.billing.OpenIabFragment;
import com.topface.framework.utils.Debug;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.data.City;
import com.topface.topface.data.CountersData;
import com.topface.topface.data.FragmentSettings;
import com.topface.topface.promo.PromoPopupManager;
import com.topface.topface.promo.dialogs.PromoExpressMessages;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.SettingsRequest;
import com.topface.topface.requests.handlers.ApiHandler;
import com.topface.topface.state.TopfaceAppState;
import com.topface.topface.statistics.TakePhotoStatistics;
import com.topface.topface.ui.dialogs.AbstractDialogFragment;
import com.topface.topface.ui.dialogs.DatingLockPopup;
import com.topface.topface.ui.dialogs.NotificationsDisablePopup;
import com.topface.topface.ui.dialogs.SetAgeDialog;
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
import com.topface.topface.utils.controllers.ChosenStartAction;
import com.topface.topface.utils.controllers.DatingInstantMessageController;
import com.topface.topface.utils.controllers.SequencedStartAction;
import com.topface.topface.utils.controllers.StartActionsController;
import com.topface.topface.utils.controllers.startactions.DatingLockPopupAction;
import com.topface.topface.utils.controllers.startactions.IStartAction;
import com.topface.topface.utils.controllers.startactions.InvitePopupAction;
import com.topface.topface.utils.controllers.startactions.OnNextActionListener;
import com.topface.topface.utils.controllers.startactions.TrialVipPopupAction;
import com.topface.topface.utils.gcmutils.GCMUtils;
import com.topface.topface.utils.offerwalls.OfferwallsManager;
import com.topface.topface.utils.social.AuthToken;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.subjects.BehaviorSubject;
import rx.subscriptions.CompositeSubscription;

import static com.topface.topface.ui.fragments.BaseFragment.FragmentId;
import static com.topface.topface.utils.controllers.StartActionsController.AC_PRIORITY_HIGH;
import static com.topface.topface.utils.controllers.StartActionsController.AC_PRIORITY_LOW;
import static com.topface.topface.utils.controllers.StartActionsController.AC_PRIORITY_NORMAL;

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
    private AtomicBoolean mBackPressedOnce = new AtomicBoolean(false);
    private AddPhotoHelper mAddPhotoHelper;
    private boolean mIsPhotoAsked;
    private PopupManager mPopupManager;
    private CompositeSubscription mSubscription = new CompositeSubscription();
    private BehaviorSubject<DRAWER_LAYOUT_STATE> mDrawerLayoutStateObservable;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            AddPhotoHelper.handlePhotoMessage(msg);
        }
    };

    private BroadcastReceiver mProfileUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (CacheProfile.age < App.getAppOptions().getUserAgeMin()) {
                SetAgeDialog.newInstance().show(getSupportFragmentManager(), SetAgeDialog.TAG);
            }
        }
    };

    /**
     * Перезапускает NavigationActivity, нужно например при смене языка
     *
     * @param activity активити, которое принадлежит тому же таску, что и старый NavigationActivity
     */
    public static void restartNavigationActivity(Activity activity) {
        Intent intent = new Intent(activity, NavigationActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .putExtra(GCMUtils.NEXT_INTENT, CacheProfile.getOptions().startPageFragmentSettings);
        if (App.getUserConfig().getDatingMessage().equals(CacheProfile.getOptions()
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
        App.from(getApplicationContext()).inject(this);
        Intent intent = getIntent();
        if (intent.getBooleanExtra(INTENT_EXIT, false)) {
            finish();
        }
        setNeedTransitionAnimation(false);
        super.onCreate(savedInstanceState);
        mSubscription.add(mAppState.getObservable(CountersData.class).subscribe(new Action1<CountersData>() {
            @Override
            public void call(CountersData countersData) {
                if (mNotificationController != null) {
                    mNotificationController.refreshNotificator(countersData.dialogs, countersData.mutual);
                }
            }
        }));
        mSubscription.add(mAppState.getObservable(AdjustAttribution.class).filter(new Func1<AdjustAttribution, Boolean>() {
            @Override
            public Boolean call(AdjustAttribution adjustAttribution) {
                return adjustAttribution != null;
            }
        }).subscribe(new Action1<AdjustAttribution>() {
            @Override
            public void call(AdjustAttribution adjustAttribution) {
                App.sendReferreRequest(adjustAttribution);
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
        mIsPhotoAsked = false;
    }

    @Override
    protected int getContentLayoutId() {
        return R.layout.ac_navigation;
    }

    @Override
    protected void onRegisterMandatoryStartActions(StartActionsController startActionsController) {
        super.onRegisterMandatoryStartActions(startActionsController);
        final SequencedStartAction sequencedStartAction = new SequencedStartAction(this, AC_PRIORITY_HIGH);
        final IStartAction popupsAction = new ChosenStartAction().chooseFrom(
                new TrialVipPopupAction(this, AC_PRIORITY_HIGH),
                new DatingLockPopupAction(getSupportFragmentManager(), AC_PRIORITY_HIGH,
                        new DatingLockPopup.DatingLockPopupRedirectListener() {
                            @Override
                            public void onRedirect() {
                                showFragment(FragmentId.TABBED_LIKES.getFragmentSettings());
                            }
                        })
        );
        sequencedStartAction.addAction(popupsAction);
        // fullscreen
        if (mFullscreenController != null) {
            sequencedStartAction.addAction(mFullscreenController.createFullscreenStartAction(AC_PRIORITY_LOW));
        }
        // trial vip popup
        startActionsController.registerMandatoryAction(sequencedStartAction);
    }

    @Override
    protected void onRegisterStartActions(StartActionsController startActionsController) {
        super.onRegisterStartActions(startActionsController);
        // actions after registration
        startActionsController.registerAction(createAfterRegistrationStartAction(AC_PRIORITY_HIGH));
        // show popup when services disable
        startActionsController.registerAction(new NotificationsDisablePopup(NavigationActivity.this, AC_PRIORITY_NORMAL));
        // promo popups
        PromoPopupManager promoPopupManager = new PromoPopupManager(this);
        IStartAction promoPopupsAction = new ChosenStartAction().chooseFrom(
                PromoExpressMessages.createPromoPopupStartAction(AC_PRIORITY_NORMAL, new PromoExpressMessages.PopupRedirectListener() {
                    @Override
                    public void onRedirect() {
                        showFragment(FragmentId.TABBED_DIALOGS.getFragmentSettings());
                        mDrawerLayoutStateObservable.onNext(DRAWER_LAYOUT_STATE.CLOSED);
                    }
                }),
                promoPopupManager.createPromoPopupStartAction(AC_PRIORITY_NORMAL)
        );
        startActionsController.registerAction(promoPopupsAction);
        // popups
        mPopupManager = new PopupManager(this);
        startActionsController.registerAction(new InvitePopupAction(this, AC_PRIORITY_LOW));
        startActionsController.registerAction(mPopupManager.createRatePopupStartAction(AC_PRIORITY_LOW));
        startActionsController.registerAction(mPopupManager.createOldVersionPopupStartAction(AC_PRIORITY_LOW));
    }

    private void initFullscreen() {
        mFullscreenController = new FullscreenController(this, CacheProfile.getOptions());
    }

    private void initBonusCounterConfig() {
        long lastTime = App.getUserConfig().getBonusCounterLastShowTime();
        CacheProfile.needShowBonusCounter = lastTime < CacheProfile.getOptions().bonus.timestamp;
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
        FragmentSettings currentFragment = (FragmentSettings) intent.getParcelableExtra(GCMUtils.NEXT_INTENT);
        Debug.log(PAGE_SWITCH + "show fragment from NEXT_INTENT: " + currentFragment);
        showFragment(currentFragment == null ? CacheProfile.getOptions().startPageFragmentSettings : currentFragment);
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
    protected void onProfileUpdated() {
        initBonusCounterConfig();
        // возможно что содержимое меню поменялось, надо обновить
        if (mMenuFragment != null) {
            mMenuFragment.updateAdapter();
        }
    }

    /**
     * Take photo then select city if profile is empty
     *
     * @return start action object to register
     */
    private IStartAction createAfterRegistrationStartAction(final int priority) {
        return new IStartAction() {

            @Override
            public void callInBackground() {
            }

            @Override
            public void callOnUi() {
                if (isTakePhotoApplicable()) {
                    takePhoto();
                } else if (isSelectCityApplicable()) {
                    CacheProfile.selectCity(NavigationActivity.this);
                }
            }

            @Override
            public boolean isApplicable() {
                return isTakePhotoApplicable() || isSelectCityApplicable();
            }

            @Override
            public int getPriority() {
                return priority;
            }

            @Override
            public String getActionName() {
                return "TakePhoto-SelectCity";
            }

            @Override
            public void setStartActionCallback(OnNextActionListener startActionCallback) {

            }

            private boolean isTakePhotoApplicable() {
                return !AuthToken.getInstance().isEmpty() && !App.getConfig().getUserConfig().isUserAvatarAvailable() && CacheProfile.photo == null;
            }

            private boolean isSelectCityApplicable() {
                return CacheProfile.needToSelectCity(NavigationActivity.this);
            }
        };
    }

    public void setPopupVisible(boolean visibility) {
        isPopupVisible = visibility;
    }

    @Override
    public void onBackPressed() {
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
        //Интеграция наших id юзера с AppsFlyer
        if (CacheProfile.uid > 0) {
            try {
                AppsFlyerLib.setAppUserId(Integer.toString(CacheProfile.uid));
            } catch (Exception e) {
                Debug.error(e);
            }
            Debug.log("Current User ID:" + CacheProfile.getProfile().uid);
        }
        if (mDrawerToggle != null) {
            mDrawerToggle.syncState();
        }
        /*
        Initialize Topface offerwall here to be able to start it quickly instead of PurchasesActivity
         */
        OfferwallsManager.initTfOfferwall(this, null);
        AdmobInterstitialUtils.preloadInterstitials(this);
    }

    @Override
    protected void onDestroy() {
        //Для запроса фото при следующем создании NavigationActivity
        if (mFullscreenController != null) {
            mFullscreenController.onDestroy();
        }
        mDrawerToggle = null;
        if (!mSubscription.isUnsubscribed()) {
            mSubscription.unsubscribe();
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
        AbstractDialogFragment currentPopup = mPopupManager.getCurrentDialog();
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
            switch (requestCode) {
                case CitySearchActivity.INTENT_CITY_SEARCH_AFTER_REGISTRATION:
                case CitySearchActivity.INTENT_CITY_SEARCH_ACTIVITY:
                    if (data != null) {
                        Bundle extras = data.getExtras();
                        if (extras != null) {
                            final City city = extras.getParcelable(CitySearchActivity.INTENT_CITY);
                            if (city != null) {
                                SettingsRequest request = new SettingsRequest(this);
                                request.cityid = city.id;
                                request.callback(new ApiHandler() {
                                    @Override
                                    public void success(IApiResponse response) {
                                        CacheProfile.city = city;
                                        CacheProfile.sendUpdateProfileBroadcast();
                                    }

                                    @Override
                                    public void fail(int codeError, IApiResponse response) {
                                    }
                                }).exec();
                            }
                        }
                    }
                    break;
                default:
                    super.onActivityResult(requestCode, resultCode, data);
                    break;
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
        AddPhotoHelper helper = getAddPhotoHelper();
        helper.processActivityResult(requestCode, resultCode, data);
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

    private void takePhoto() {
        if (!mIsPhotoAsked) {
            mIsPhotoAsked = true;
            startActivityForResult(TakePhotoActivity.createIntent(this, TakePhotoStatistics.PLC_AFTER_REGISTRATION_ACTION), TakePhotoActivity.REQUEST_CODE_TAKE_PHOTO);
        }
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
