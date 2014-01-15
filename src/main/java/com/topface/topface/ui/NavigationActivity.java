package com.topface.topface.ui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.topface.billing.BillingUtils;
import com.topface.topface.App;
import com.topface.topface.GCMUtils;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.data.City;
import com.topface.topface.data.Photo;
import com.topface.topface.promo.PromoPopupManager;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.PhotoMainRequest;
import com.topface.topface.requests.SettingsRequest;
import com.topface.topface.requests.handlers.ApiHandler;
import com.topface.topface.requests.handlers.ErrorCodes;
import com.topface.topface.ui.dialogs.TakePhotoDialog;
import com.topface.topface.ui.fragments.MenuFragment;
import com.topface.topface.ui.profile.PhotoSwitcherActivity;
import com.topface.topface.ui.settings.SettingsContainerActivity;
import com.topface.topface.ui.views.HackyDrawerLayout;
import com.topface.topface.utils.BackgroundThread;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.CountersManager;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.ExternalLinkExecuter;
import com.topface.topface.utils.FullscreenController;
import com.topface.topface.utils.LocaleConfig;
import com.topface.topface.utils.NavigationBarController;
import com.topface.topface.utils.Novice;
import com.topface.topface.utils.PopupManager;
import com.topface.topface.utils.controllers.AbstractStartAction;
import com.topface.topface.utils.controllers.IStartAction;
import com.topface.topface.utils.controllers.StartActionsController;
import com.topface.topface.utils.offerwalls.Offerwalls;
import com.topface.topface.utils.social.AuthToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static com.topface.topface.ui.fragments.BaseFragment.FragmentId;
import static com.topface.topface.utils.controllers.StartActionsController.AC_PRIORITY_HIGH;
import static com.topface.topface.utils.controllers.StartActionsController.AC_PRIORITY_LOW;
import static com.topface.topface.utils.controllers.StartActionsController.AC_PRIORITY_NORMAL;

public class NavigationActivity extends CustomTitlesBaseFragmentActivity implements TakePhotoDialog.ITakePhotoListener {
    public static final String FROM_AUTH = "com.topface.topface.AUTH";
    public static final String BONUS_COUNTER_TAG = "preferences_for_bonus_counter";
    public static final String BONUS_COUNTER_LAST_SHOW_TIME = "last_show_time";

    private MenuFragment mMenuFragment;
    private HackyDrawerLayout mDrawerLayout;
    private FullscreenController mFullscreenController;

    private SharedPreferences mPreferences;
    private boolean needAnimate = false;
    private boolean isPopupVisible = false;

    private static NavigationActivity instance = null;
    private ActionBarDrawerToggle mDrawerToggle;
    private NavigationBarController mNavBarController;

    private BroadcastReceiver mCountersReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (mNavBarController != null) mNavBarController.refreshNotificators();
        }
    };

    public void setTakePhotoDialogStarted(boolean takePhotoDialogStarted) {
        this.takePhotoDialogStarted = takePhotoDialogStarted;
    }

    private boolean takePhotoDialogStarted;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        mNeedAnimate = false;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_navigation);
        instance = this;
        if (isNeedBroughtToFront(getIntent())) {
            // При открытии активити из лаунчера перезапускаем ее
            finish();
            return;
        }
        initDrawerLayout();
        initFullscreen();
        new BackgroundThread() {
            @Override
            public void execute() {
                onCreateAsync();
            }
        };
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
        PopupManager popupManager = new PopupManager(this);
        startActionsController.registerAction(popupManager.createRatePopupStartAction(AC_PRIORITY_LOW));
        startActionsController.registerAction(popupManager.createOldVersionPopupStartAction(AC_PRIORITY_LOW));
        startActionsController.registerAction(popupManager.createInvitePopupStartAction(AC_PRIORITY_LOW));
        // fullscreen
        startActionsController.registerAction(mFullscreenController.createFullscreenStartAction(AC_PRIORITY_LOW));
    }

    private void initFullscreen() {
        mFullscreenController = new FullscreenController(this);
    }

    public boolean getDialogStarted() {
        return takePhotoDialogStarted;
    }

    @Override
    protected void initCustomActionBarView(View mCustomView) {
        mNavBarController = new NavigationBarController(
                (ViewGroup) getCustomActionBarView().findViewById(R.id.loCounters)
        );
    }

    @Override
    protected int getActionBarCustomViewResId() {
        return R.layout.actionbar_navigation_title_view;
    }

    protected void onCreateAsync() {
        Novice.getInstance(getPreferences()).initNoviceFlags();
    }

    private void initBonusCounterConfig() {
        SharedPreferences preferences = getSharedPreferences(BONUS_COUNTER_TAG, Context.MODE_PRIVATE);
        long lastTime = preferences.getLong(BONUS_COUNTER_LAST_SHOW_TIME, 0);
        CacheProfile.NEED_SHOW_BONUS_COUNTER = lastTime < CacheProfile.getOptions().bonus.timestamp;
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

        //Если активити открыто с указанием фрагмента, который нужно открыть
        /*Intent intent = getIntent();
        if (intent.hasExtra(GCMUtils.NEXT_INTENT)) {
            showFragment(intent);
        }*/


        mDrawerLayout = (HackyDrawerLayout) findViewById(R.id.loNavigationDrawer);
        mDrawerLayout.setScrimColor(Color.argb(217, 0, 0, 0));
        mDrawerLayout.setDrawerShadow(R.drawable.shadow_left_menu_right, GravityCompat.START);
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.drawable.ic_drawer,  /* nav drawer icon to replace 'Up' caret */
                R.string.app_name,  /* "open drawer" description */
                R.string.app_name  /* "close drawer" description */
        );
        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);
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

    private SharedPreferences getPreferences() {
        if (mPreferences == null) {
            mPreferences = getSharedPreferences(Static.PREFERENCES_TAG_SHARED, Context.MODE_PRIVATE);
        }
        return mPreferences;
    }

    public void showFragment(FragmentId fragmentId) {
        mMenuFragment.selectMenu(fragmentId);
    }

    private void showFragment(Intent intent) {
        //Получаем id фрагмента, если он открыт
        FragmentId currentFragment = (FragmentId) intent.getSerializableExtra(GCMUtils.NEXT_INTENT);
        showFragment(currentFragment == null ? FragmentId.F_DATING : currentFragment);
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
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        showFragment(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //restart -> open NavigationActivity
        if (App.getConfig().getLocaleConfig().fetchToSystemLocale()) {
            LocaleConfig.changeLocale(this, App.getConfig().getLocaleConfig().getApplicationLocale());
            return;
        } else {
            LocaleConfig.localeChangeInitiated = false;
        }

        //Отправляем не обработанные запросы на покупку
        BillingUtils.sendQueueItems();

        if (needAnimate) {
            overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_right);
        }

        needAnimate = true;

        //Если перешли в приложение по ссылке, то этот класс смотрит что за ссылка и делает то что нужно
        new ExternalLinkExecuter(mListener).execute(getIntent());

        App.checkProfileUpdate();

        if (mNavBarController != null) {
            mNavBarController.refreshNotificators();
        }
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(mCountersReceiver, new IntentFilter(CountersManager.UPDATE_COUNTERS));
    }

    @Override
    protected void onProfileUpdated() {
        initBonusCounterConfig();
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
                if (!AuthToken.getInstance().isEmpty()) {
                    if (CacheProfile.photo == null) {
                        mTakePhotoApplicable = true;
                        return true;
                    }
                }
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
        mMenuFragment.onLoadProfile();
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        super.startActivityForResult(intent, requestCode);

    }

    @Override
    protected void onDestroy() {
        //Для запроса фото при следующем создании NavigationActivity
        if (CacheProfile.photo == null) CacheProfile.wasAvatarAsked = false;
        if (mFullscreenController != null) {
            mFullscreenController.onDestroy();
        }
        super.onDestroy();
    }

    @Override
    public boolean isTrackable() {
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == CitySearchActivity.INTENT_CITY_SEARCH_AFTER_REGISTRATION ||
                    requestCode == CitySearchActivity.INTENT_CITY_SEARCH_ACTIVITY) {
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
            }
        }
    }

    ExternalLinkExecuter.OnExternalLinkListener mListener = new ExternalLinkExecuter.OnExternalLinkListener() {
        @Override
        public void onProfileLink(int profileID) {
            startActivity(ContainerActivity.getProfileIntent(profileID, NavigationActivity.this));
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
            Offerwalls.startOfferwall(NavigationActivity.this);
            getIntent().setData(null);
        }
    };

    public static void onLogout() {
        MenuFragment.onLogout();
    }

    public static void restartNavigationActivity(FragmentId fragmentId) {
        Activity activity = instance;
        Intent intent = new Intent(activity, NavigationActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .putExtra(GCMUtils.NEXT_INTENT, fragmentId);
        activity.startActivity(intent);
        activity.finish();
    }

    @Override
    public boolean onKeyDown(int keycode, KeyEvent e) {
        switch (keycode) {
            case KeyEvent.KEYCODE_MENU:
                if (mDrawerLayout.getDrawerLockMode(GravityCompat.START) ==
                        DrawerLayout.LOCK_MODE_UNLOCKED) {
                    if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                        mDrawerLayout.closeDrawer(GravityCompat.START);
                    } else {
                        mDrawerLayout.openDrawer(GravityCompat.START);
                    }
                }
                return true;
        }

        return super.onKeyDown(keycode, e);
    }

    @Override
    public void onTakePhotoDialogSentSuccess(final Photo photo) {
        if (CacheProfile.photos != null) {
            CacheProfile.photos.add(photo);
            Intent intent = new Intent(PhotoSwitcherActivity.DEFAULT_UPDATE_PHOTOS_INTENT);
            intent.putExtra(PhotoSwitcherActivity.INTENT_CLEAR, true);
            intent.putExtra(PhotoSwitcherActivity.INTENT_PHOTOS, CacheProfile.photos);
            LocalBroadcastManager.getInstance(NavigationActivity.this).sendBroadcast(intent);
        } else {
            Intent intent = new Intent(PhotoSwitcherActivity.DEFAULT_UPDATE_PHOTOS_INTENT);
            ArrayList<Photo> photos = new ArrayList<>();
            photos.add(photo);
            intent.putParcelableArrayListExtra(PhotoSwitcherActivity.INTENT_PHOTOS, photos);
        }
        takePhotoDialogStarted = false;
        PhotoMainRequest request = new PhotoMainRequest(getApplicationContext());
        request.photoid = photo.getId();
        request.callback(new ApiHandler() {

            @Override
            public void success(IApiResponse response) {
                CacheProfile.photo = photo;
                CacheProfile.sendUpdateProfileBroadcast();
            }

            @Override
            public void fail(int codeError, IApiResponse response) {
                if (codeError == ErrorCodes.NON_EXIST_PHOTO_ERROR) {
                    if (CacheProfile.photos != null && CacheProfile.photos.contains(photo)) {
                        CacheProfile.photos.remove(photo);
                    }
                    Toast.makeText(
                            NavigationActivity.this,
                            App.getContext().getString(R.string.general_wrong_photo_upload),
                            Toast.LENGTH_LONG
                    ).show();
                }
            }

            @Override
            public void always(IApiResponse response) {
                super.always(response);
            }
        }).exec();
        needOpenDialog = true;
    }

    @Override
    public void onTakePhotoDialogSentFailure() {
        Toast.makeText(App.getContext(), R.string.photo_add_error, Toast.LENGTH_SHORT).show();
        needOpenDialog = true;
    }

    @Override
    public void onTakePhotoDialogDismiss() {
        takePhotoDialogStarted = false;
        if (CacheProfile.needToSelectCity(NavigationActivity.this)) {
            CacheProfile.selectCity(NavigationActivity.this);
        }
    }
}
