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
import android.widget.ImageView;
import android.widget.Toast;

import com.appsflyer.AppsFlyerLib;
import com.topface.billing.OpenIabFragment;
import com.topface.framework.utils.Debug;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.data.City;
import com.topface.topface.promo.PromoPopupManager;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.SettingsRequest;
import com.topface.topface.requests.handlers.ApiHandler;
import com.topface.topface.ui.blocks.FloatBlock;
import com.topface.topface.ui.dialogs.AbstractDialogFragment;
import com.topface.topface.ui.dialogs.DatingLockPopup;
import com.topface.topface.ui.fragments.MenuFragment;
import com.topface.topface.ui.fragments.profile.DatingLockPopupAction;
import com.topface.topface.ui.fragments.profile.OwnProfileFragment;
import com.topface.topface.ui.settings.SettingsContainerActivity;
import com.topface.topface.ui.views.HackyDrawerLayout;
import com.topface.topface.utils.AddPhotoHelper;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.CountersManager;
import com.topface.topface.utils.CustomViewNotificationController;
import com.topface.topface.utils.ExternalLinkExecuter;
import com.topface.topface.utils.IActionbarNotifier;
import com.topface.topface.utils.IconNotificationController;
import com.topface.topface.utils.LocaleConfig;
import com.topface.topface.utils.PhotoTaker;
import com.topface.topface.utils.PopupManager;
import com.topface.topface.utils.Utils;
import com.topface.topface.utils.ads.FullscreenController;
import com.topface.topface.utils.controllers.AbstractStartAction;
import com.topface.topface.utils.controllers.IStartAction;
import com.topface.topface.utils.controllers.StartActionsController;
import com.topface.topface.utils.gcmutils.GCMUtils;
import com.topface.topface.utils.offerwalls.OfferwallsManager;
import com.topface.topface.utils.social.AuthToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.topface.topface.ui.fragments.BaseFragment.FragmentId;
import static com.topface.topface.utils.controllers.StartActionsController.AC_PRIORITY_HIGH;
import static com.topface.topface.utils.controllers.StartActionsController.AC_PRIORITY_LOW;
import static com.topface.topface.utils.controllers.StartActionsController.AC_PRIORITY_NORMAL;

public class NavigationActivity extends BaseFragmentActivity implements INavigationFragmentsListener {
    public static final String OPEN_MENU = "com.topface.topface.open.menu";
    public static final String FROM_AUTH = "com.topface.topface.AUTH";
    public static final String INTENT_EXIT = "EXIT";
    public static final String PAGE_SWITCH = "Page switch: ";

    private Intent mPendingNextIntent;
    ExternalLinkExecuter.OnExternalLinkListener mListener = new ExternalLinkExecuter.OnExternalLinkListener() {
        @Override
        public void onProfileLink(int profileID) {
            startActivity(UserProfileActivity.createIntent(profileID, NavigationActivity.this));
            getIntent().setData(null);
        }

        @Override
        public void onConfirmLink(String code) {
            AuthToken token = AuthToken.getInstance();
            if (!token.isEmpty() && token.getSocialNet().equals(AuthToken.SN_TOPFACE)) {
                Intent intent = new Intent(NavigationActivity.this, SettingsContainerActivity.class);
                intent.putExtra(Static.INTENT_REQUEST_KEY, SettingsContainerActivity.INTENT_ACCOUNT);
                intent.putExtra(SettingsContainerActivity.CONFIRMATION_CODE, code);
                startActivity(intent);
            }
            getIntent().setData(null);
        }

        @Override
        public void onOfferWall() {
            OfferwallsManager.startOfferwall(NavigationActivity.this);
            getIntent().setData(null);
        }
    };
    private boolean mIsActionBarHidden;
    private View mContentFrame;
    private MenuFragment mMenuFragment;
    private HackyDrawerLayout mDrawerLayout;
    private FullscreenController mFullscreenController;
    private boolean isPopupVisible = false;
    private boolean mActionBarOverlayed = false;
    private int mInitialTopMargin = 0;
    private ActionBarDrawerToggle mDrawerToggle;
    private IActionbarNotifier mNotificationController;
    private BroadcastReceiver mCountersReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (mNotificationController != null) mNotificationController.refreshNotificator();
        }
    };
    private BroadcastReceiver mOpenMenuReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (mMenuFragment.isLockedByClosings()) {
                mMenuFragment.showClosingsDialog();
            } else {
                toggleDrawerLayout();
            }
        }
    };
    private AtomicBoolean mBackPressedOnce = new AtomicBoolean(false);
    private AddPhotoHelper mAddPhotoHelper;
    private PopupManager mPopupManager;

    public static void onLogout() {
        MenuFragment.onLogout();
    }

    /**
     * Перезапускает NavigationActivity, нужно например при смене языка
     *
     * @param activity активити, которое принадлежит тому же таску, что и старый NavigationActivity
     */
    public static void restartNavigationActivity(Activity activity) {
        Intent intent = new Intent(activity, NavigationActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .putExtra(GCMUtils.NEXT_INTENT, CacheProfile.getOptions().startPageFragmentId);
        activity.startActivity(intent);
    }

    @Override
    protected void initActionBar(ActionBar actionBar) {
        super.initActionBar(actionBar);
        if (actionBar != null) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
                View customView = actionBar.getCustomView();
                if (customView != null) {
                    View upIcon = customView.findViewById(R.id.up_icon);
                    if (upIcon instanceof ImageView) {
                        ((ImageView) upIcon).setImageResource(R.drawable.ic_home);
                    }
                }
                mNotificationController = new CustomViewNotificationController(actionBar);
            } else {
                actionBar.setLogo(R.drawable.ic_home);
                actionBar.setDisplayUseLogoEnabled(true);
                mNotificationController = new IconNotificationController(actionBar);
            }
            mNotificationController.refreshNotificator();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();
        if (intent.getBooleanExtra(INTENT_EXIT, false)) {
            finish();
        }
        setNeedTransitionAnimation(false);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_navigation);
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
    }

    @Override
    protected void onRegisterStartActions(StartActionsController startActionsController) {
        super.onRegisterStartActions(startActionsController);
        // actions after registration
        startActionsController.registerAction(createAfterRegistrationStartAction(AC_PRIORITY_HIGH));
        // promo popups
        PromoPopupManager promoPopupManager = new PromoPopupManager(this);
        startActionsController.registerAction(promoPopupManager.createPromoPopupStartAction(AC_PRIORITY_NORMAL));
        // popups
        mPopupManager = new PopupManager(this);
        startActionsController.registerAction(new DatingLockPopupAction(getSupportFragmentManager(), AC_PRIORITY_NORMAL, new DatingLockPopup.DatingLockPopupRedirectListener() {
            @Override
            public void onRedirect() {
                showFragment(FragmentId.LIKES);
            }
        }));
        startActionsController.registerAction(mPopupManager.createRatePopupStartAction(AC_PRIORITY_LOW));
        startActionsController.registerAction(mPopupManager.createOldVersionPopupStartAction(AC_PRIORITY_LOW));
        startActionsController.registerAction(mPopupManager.createInvitePopupStartAction(AC_PRIORITY_LOW));
        // fullscreen
        if (mFullscreenController != null) {
            startActionsController.registerMandatoryAction(mFullscreenController.createFullscreenStartAction(AC_PRIORITY_LOW));
        }
    }

    private void initFullscreen() {
        mFullscreenController = new FullscreenController(this);
    }

    private void initBonusCounterConfig() {
        long lastTime = App.getUserConfig().getBonusCounterLastShowTime();
        CacheProfile.needShowBonusCounter = lastTime < CacheProfile.getOptions().bonus.timestamp;
    }

    private void initDrawerLayout() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        mMenuFragment = (MenuFragment) fragmentManager.findFragmentById(R.id.fragment_menu);
        if (mMenuFragment == null) {
            mMenuFragment = new MenuFragment();
        }
        mMenuFragment.setOnFragmentSelected(new MenuFragment.OnFragmentSelectedListener() {
            @Override
            public void onFragmentSelected(FragmentId fragmentId) {
                if (mDrawerLayout.getDrawerLockMode(GravityCompat.START) ==
                        DrawerLayout.LOCK_MODE_UNLOCKED) {
                    mDrawerLayout.closeDrawer(GravityCompat.START);
                }
            }
        });
        if (!mMenuFragment.isAdded()) {
            fragmentManager
                    .beginTransaction()
                    .add(R.id.fragment_menu, mMenuFragment)
                    .commit();
        }
        mDrawerLayout = (HackyDrawerLayout) findViewById(R.id.loNavigationDrawer);
        mDrawerLayout.setScrimColor(Color.argb(217, 0, 0, 0));
        mDrawerLayout.setDrawerShadow(R.drawable.shadow_left_menu_right, GravityCompat.START);
        mDrawerToggle = new ActionBarDrawerToggle(
                this, /* host Activity */
                mDrawerLayout, /* DrawerLayout object */
                Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2 ? android.R.color.transparent : R.drawable.empty_home_as_up, /* nav drawer icon to replace 'Up' caret */
                R.string.app_name, /* "open drawer" description */
                R.string.app_name /* "close drawer" description */
        ) {
            @Override
            public void onDrawerStateChanged(int newState) {
                super.onDrawerStateChanged(newState);
                Utils.hideSoftKeyboard(NavigationActivity.this, mDrawerLayout.getWindowToken());
            }
        };
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
                    if (mMenuFragment.isLockedByClosings()) {
                        mMenuFragment.showClosingsDialog();
                    }
                    return true;
                default:
                    return super.onOptionsItemSelected(item);
            }
        }
    }

    public void showFragment(FragmentId fragmentId) {
        Debug.log(PAGE_SWITCH + "show fragment: " + fragmentId);
        mMenuFragment.selectMenu(fragmentId);
    }

    private void showFragment(Intent intent) {
        //Получаем id фрагмента, если он открыт
        FragmentId currentFragment = (FragmentId) intent.getSerializableExtra(GCMUtils.NEXT_INTENT);
        Debug.log(PAGE_SWITCH + "show fragment from NEXT_INTENT: " + currentFragment);
        showFragment(currentFragment == null ? CacheProfile.getOptions().startPageFragmentId : currentFragment);
    }

    public void showContent() {
        mDrawerLayout.openDrawer(GravityCompat.START);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mFullscreenController != null) {
            mFullscreenController.onPause();
        }
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mCountersReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mOpenMenuReceiver);
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
        //restart -> open NavigationActivity
        if (App.getLocaleConfig().fetchToSystemLocale()) {
            LocaleConfig.changeLocale(this, App.getLocaleConfig().getApplicationLocale());
            return;
        } else {
            LocaleConfig.localeChangeInitiated = false;
        }
        //Если перешли в приложение по ссылке, то этот класс смотрит что за ссылка и делает то что нужно
        new ExternalLinkExecuter(mListener).execute(getIntent());
        App.checkProfileUpdate();
        if (mNotificationController != null) {
            mNotificationController.refreshNotificator();
        }
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(mCountersReceiver, new IntentFilter(CountersManager.UPDATE_COUNTERS));
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(mOpenMenuReceiver, new IntentFilter(OPEN_MENU));
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
        if (mMenuFragment != null && !mMenuFragment.isClosingsAvailable()) {
            mMenuFragment.updateAdapter();
        }
        FloatBlock.resetActivityMap();
        mNotificationController.refreshNotificator();
    }

    /**
     * Take photo then select city if profile is empty
     *
     * @return start action object to register
     */
    private IStartAction createAfterRegistrationStartAction(final int priority) {
        return new AbstractStartAction() {
            private boolean mTakePhotoApplicable = false;
            private boolean mSelectCityApplicable = false;

            @Override
            public void callInBackground() {
            }

            @Override
            public void callOnUi() {
                if (mTakePhotoApplicable) {
                    takePhoto();
                } else if (mSelectCityApplicable) {
                    CacheProfile.selectCity(NavigationActivity.this);
                }
            }

            @Override
            public boolean isApplicable() {
                mTakePhotoApplicable = !AuthToken.getInstance().isEmpty() && (CacheProfile.photo == null);
                mSelectCityApplicable = CacheProfile.needToSelectCity(NavigationActivity.this);
                return mTakePhotoApplicable || mSelectCityApplicable;
            }

            @Override
            public int getPriority() {
                return priority;
            }

            @Override
            public String getActionName() {
                return "TakePhoto-SelectCity";
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
        } else if (mMenuFragment.isLockedByClosings()) {
            mMenuFragment.showClosingsDialog();
        } else if (!mBackPressedOnce.get()) {
            (new Timer()).schedule(new TimerTask() {
                @Override
                public void run() {
                    mBackPressedOnce.set(false);
                }
            }, 3000);
            mBackPressedOnce.set(true);
            Toast.makeText(App.getContext(), R.string.press_back_more_to_close_app, Toast.LENGTH_SHORT).show();
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
            if (lockMode == DrawerLayout.LOCK_MODE_UNLOCKED && mMenuFragment.isLockedByClosings()) {
                return;
            }
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
        mDrawerToggle.syncState();
        mMenuFragment.onLoadProfile();

        /*
        Initialize Topface offerwall here to be able to start it quickly instead of PurchasesActivity
         */
        OfferwallsManager.initTfOfferwall(this, null);
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        super.startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onDestroy() {
        //Для запроса фото при следующем создании NavigationActivity
        if (mFullscreenController != null) {
            mFullscreenController.onDestroy();
        }
        mDrawerToggle = null;
        super.onDestroy();
    }

    @Override
    public boolean isTrackable() {
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
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
                        try {
                            if (extras != null) {
                                final City city = new City(new JSONObject(extras.getString(CitySearchActivity.INTENT_CITY)));
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
                        } catch (JSONException e) {
                            Debug.error(e);
                        }
                    }
                    break;
                case AddPhotoHelper.GALLERY_IMAGE_ACTIVITY_REQUEST_CODE_LIBRARY_WITH_DIALOG:
                case AddPhotoHelper.GALLERY_IMAGE_ACTIVITY_REQUEST_CODE_CAMERA_WITH_DIALOG:
                    AddPhotoHelper helper = getAddPhotoHelper();
                    helper.showTakePhotoDialog(new PhotoTaker(helper, this), helper.processActivityResult(requestCode, resultCode, data, false));
                    break;
            }
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
        }
        return mAddPhotoHelper;
    }

    private void takePhoto() {
        getAddPhotoHelper().showTakePhotoDialog(new PhotoTaker(getAddPhotoHelper(), this), null);
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
    public void onFragmentSwitch(FragmentId fragmentId) {
        if (fragmentId.isOverlayed()) {
            switchContentTopMargin(true);
        } else if (mActionBarOverlayed) {
            switchContentTopMargin(false);
        }
    }

    @Override
    public void onHideActionBar() {
        if (!mMenuFragment.isLockedByClosings()) {
            mIsActionBarHidden = true;
            setMenuLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            getSupportActionBar().hide();
        }
    }

    @Override
    public void onShowActionBar() {
        mIsActionBarHidden = false;
        setMenuLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        getSupportActionBar().show();
    }
}
