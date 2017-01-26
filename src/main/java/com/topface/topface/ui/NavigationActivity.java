package com.topface.topface.ui;

import android.app.Activity;
import android.content.Intent;
import android.databinding.ViewDataBinding;
import android.os.BadParcelableException;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.appsflyer.AppsFlyerLib;
import com.topface.framework.utils.Debug;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.data.City;
import com.topface.topface.data.Options;
import com.topface.topface.data.Profile;
import com.topface.topface.data.leftMenu.DrawerLayoutStateData;
import com.topface.topface.data.leftMenu.LeftMenuSettingsData;
import com.topface.topface.data.leftMenu.NavigationState;
import com.topface.topface.data.leftMenu.WrappedNavigationData;
import com.topface.topface.databinding.AcNavigationBinding;
import com.topface.topface.databinding.AcNewNavigationBinding;
import com.topface.topface.databinding.ToolbarBinding;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.SettingsRequest;
import com.topface.topface.requests.handlers.ApiHandler;
import com.topface.topface.state.DrawerLayoutState;
import com.topface.topface.state.TopfaceAppState;
import com.topface.topface.ui.dialogs.NotificationsDisableStartAction;
import com.topface.topface.ui.dialogs.SetAgeDialog;
import com.topface.topface.ui.external_libs.adjust.AdjustAttributeData;
import com.topface.topface.ui.fragments.IOnBackPressed;
import com.topface.topface.ui.fragments.MenuFragment;
import com.topface.topface.ui.views.DrawerLayoutManager;
import com.topface.topface.ui.views.HackyDrawerLayout;
import com.topface.topface.ui.views.toolbar.toolbar_custom_view.CustomToolbarViewModel;
import com.topface.topface.ui.views.toolbar.utils.ToolbarSettingsData;
import com.topface.topface.ui.views.toolbar.view_models.BaseToolbarViewModel;
import com.topface.topface.ui.views.toolbar.view_models.DatingRedesignToolbarViewModel;
import com.topface.topface.ui.views.toolbar.view_models.NavigationToolbarViewModel;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.ISimpleCallback;
import com.topface.topface.utils.NavigationManager;
import com.topface.topface.utils.Utils;
import com.topface.topface.utils.ads.AdmobInterstitialUtils;
import com.topface.topface.utils.ads.FullscreenController;
import com.topface.topface.utils.config.UserConfig;
import com.topface.topface.utils.config.WeakStorage;
import com.topface.topface.utils.controllers.DatingInstantMessageController;
import com.topface.topface.utils.controllers.startactions.DatingLockPopupAction;
import com.topface.topface.utils.controllers.startactions.ExpressMessageAction;
import com.topface.topface.utils.controllers.startactions.TrialVipPopupAction;
import com.topface.topface.utils.gcmutils.GCMUtils;
import com.topface.topface.utils.popups.PopupManager;
import com.topface.topface.utils.popups.PopupSequence;
import com.topface.topface.utils.popups.start_actions.ChooseCityPopupAction;
import com.topface.topface.utils.popups.start_actions.OldVersionStartAction;
import com.topface.topface.utils.popups.start_actions.PromoPopupStartAction;
import com.topface.topface.utils.popups.start_actions.RatePopupStartAction;
import com.topface.topface.utils.popups.start_actions.SelectPhotoStartAction;
import com.topface.topface.utils.social.AuthToken;
import com.topface.topface.viewModels.RedesignedNavigationActivityViewModel;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;

import rx.functions.Action1;
import rx.functions.Func1;
import rx.subscriptions.CompositeSubscription;

public class NavigationActivity extends ParentNavigationActivity<ViewDataBinding> implements INavigationFragmentsListener {
    public static final String INTENT_EXIT = "com.topface.topface.is_user_banned";
    private static final String PAGE_SWITCH = "Page switch: ";
    public static final String FRAGMENT_SETTINGS = "fragment_settings";
    private static final int EXIT_TIMEOUT = 3000;

    public static final String NAVIGATION_ACTIVITY_POPUPS_TAG = NavigationActivity.class.getSimpleName();

    private Intent mPendingNextIntent;
    private boolean mIsActionBarHidden;
    private View mContentFrame;
    private DrawerLayoutManager<HackyDrawerLayout> mDrawerLayout;
    private FullscreenController mFullscreenController;
    private boolean mIsPopupVisible = false;
    private boolean mActionBarOverlayed = false;
    private int mInitialTopMargin = 0;
    @Inject
    TopfaceAppState mAppState;
    @Inject
    NavigationState mNavigationState;
    @Inject
    DrawerLayoutState mDrawerLayoutState;
    @Inject
    WeakStorage mWeakStorage;
    private AtomicBoolean mBackPressedOnce = new AtomicBoolean(false);
    public static boolean isPhotoAsked;
    private CompositeSubscription mSubscription = new CompositeSubscription();
    private NavigationManager mNavigationManager;
    public static boolean hasNewOptionsOrProfile = false;

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
        setViewBinding();
        mSubscription.add(mAppState.getObservable(AdjustAttributeData.class).subscribe(new Action1<AdjustAttributeData>() {
            @Override
            public void call(AdjustAttributeData adjustAttributionData) {
                App.sendAdjustAttributeData(adjustAttributionData);
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
                if (drawerLayoutStateData.getState() != DrawerLayoutStateData.UNDEFINED && mDrawerLayout != null &&
                        mDrawerLayout.getDrawer() != null) {
                    Utils.hideSoftKeyboard(NavigationActivity.this, mDrawerLayout.getDrawer().getWindowToken());
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
    }

    private void setViewBinding() {
        ViewDataBinding binding = getViewBinding();
        if (binding instanceof AcNewNavigationBinding) {
            ((AcNewNavigationBinding) binding).setViewModel(new RedesignedNavigationActivityViewModel());

        }
    }

    @NotNull
    @Override
    protected BaseToolbarViewModel generateToolbarViewModel(@NotNull ToolbarBinding toolbar) {
        return mWeakStorage.getDatingRedesignEnabled() ?
                new DatingRedesignToolbarViewModel(toolbar, this) :
                new NavigationToolbarViewModel(toolbar, this);
    }

    private void initPopups() {
        Debug.log("PopupMANAGER init");
        PopupManager popupManager = PopupManager.INSTANCE;
        popupManager.init(this);
        popupManager.registerSequence(NAVIGATION_ACTIVITY_POPUPS_TAG, createPopupSequence());
        popupManager.runSequence(NAVIGATION_ACTIVITY_POPUPS_TAG);

    }

    public PopupSequence createPopupSequence() {
        return new PopupSequence()
                .addAction(OldVersionStartAction.class)
                .addAction(ExpressMessageAction.class)
                .addChosenAction(TrialVipPopupAction.class, DatingLockPopupAction.class)
                .addAction(FullscreenController.class)
                .addChosenAction(SelectPhotoStartAction.class, ChooseCityPopupAction.class)
                .addAction(NotificationsDisableStartAction.class)
                .addAction(PromoPopupStartAction.class)
                //TODO Отключаем до момента поддержки пермишинов на контакты
                //.addAction(InvitePopupAction.class)
                .addAction(RatePopupStartAction.class);
    }

    private void startPopupRush() {
        Debug.log("PopupMANAGER start rusgh");
        if (hasNewOptionsOrProfile && !App.get().getProfile().isFromCache
                && App.get().isUserOptionsObtainedFromServer()
                && !CacheProfile.isEmpty() && !AuthToken.getInstance().isEmpty()) {
            initPopups();
        }
    }

    private void initFullscreen() {
        mFullscreenController = new FullscreenController(this);
    }

    @Nullable
    public FullscreenController getFullscreenController() {
        return mFullscreenController;
    }

    public NavigationManager getNavigationManager() {
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
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(FRAGMENT_SETTINGS, getNavigationManager().getCurrentFragmentSettings());
        mFullscreenController.onSaveInstanceState(outState);
        PopupManager.INSTANCE.saveState();
    }

    @Override
    protected void onRestoreInstanceState(@NotNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mFullscreenController.onRestoreInstanceState(savedInstanceState);
    }

    private void initDrawerLayout() {
        getNavigationManager().init();
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
        //Если активити создалась заново(переворот), то нужно контекст заменить на актульный
        PopupManager.INSTANCE.init(this);
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        PopupManager.INSTANCE.restoreState();
        tryPostponedStartFragment();
        if (mFullscreenController != null) {
            mFullscreenController.onResume();
            if (PopupManager.INSTANCE.isSequenceComplete(NAVIGATION_ACTIVITY_POPUPS_TAG) &&
                    mFullscreenController.canShowFullscreen() && !AuthToken.getInstance().isEmpty()) {
                mFullscreenController.requestFullscreen();
            }
        }
        App.checkProfileUpdate();
    }

    @Override
    protected void onOptionsUpdated() {
        Debug.log("PopupMANAGER opt");
        hasNewOptionsOrProfile = true;
        startPopupRush();
    }

    @Override
    protected void onProfileUpdated() {
        super.onProfileUpdated();
        Debug.log("PopupMANAGER prof");
        hasNewOptionsOrProfile = true;
        if (App.get().getProfile().age < App.getAppOptions().getUserAgeMin()) {
            SetAgeDialog.newInstance().show(getSupportFragmentManager(), SetAgeDialog.TAG);
        }
        startPopupRush();
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
        if (mSubscription != null && !mSubscription.isUnsubscribed()) {
            mSubscription.unsubscribe();
        }
        PopupManager.INSTANCE.release();
        super.onDestroy();
        AdmobInterstitialUtils.releaseInterstitials();
    }

    @Override
    public boolean isTrackable() {
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Utils.activityResultToNestedFragments(getSupportFragmentManager(), requestCode, resultCode, data);
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

    @NotNull
    @Override
    public ToolbarBinding getToolbarBinding(@NotNull ViewDataBinding binding) {
        return mWeakStorage.getDatingRedesignEnabled() ? ((AcNewNavigationBinding) binding).navigationAppBar.toolbarInclude :
                ((AcNavigationBinding) binding).navigationAppBar.toolbarInclude;
    }

    @Override
    public int getLayout() {
        return mWeakStorage.getDatingRedesignEnabled() ? R.layout.ac_new_navigation : R.layout.ac_navigation;
    }

    @Override
    public void setToolbarSettings(@NotNull ToolbarSettingsData settings) {
        if (getToolbarViewModel() instanceof NavigationToolbarViewModel) {
            NavigationToolbarViewModel toolbarViewModel = (NavigationToolbarViewModel) getToolbarViewModel();
            CustomToolbarViewModel customViewModel = toolbarViewModel.getExtraViewModel();
            if (customViewModel != null) {
                if (toolbarViewModel.isScrimVisible().get()) {
                    customViewModel.getTitleVisibility().set(TextUtils.isEmpty(settings.getTitle()) ? View.GONE : View.VISIBLE);
                    customViewModel.getSubTitleVisibility().set(TextUtils.isEmpty(settings.getSubtitle()) ? View.GONE : View.VISIBLE);
                }
                Boolean isOnline = settings.isOnline();
                customViewModel.isOnline().set(isOnline != null && isOnline);
                if (settings.getTitle() != null) {
                    customViewModel.getTitle().set(settings.getTitle());
                }
                if (settings.getSubtitle() != null) {
                    customViewModel.getSubTitle().set(settings.getSubtitle());
                }
            }
        }
    }
}
