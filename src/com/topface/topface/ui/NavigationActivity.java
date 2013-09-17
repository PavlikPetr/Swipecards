package com.topface.topface.ui;

import android.app.Activity;
import android.content.*;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.internal.widget.ActionBarContainer;
import android.view.*;
import android.widget.Toast;
import com.topface.billing.BillingUtils;
import com.topface.topface.App;
import com.topface.topface.GCMUtils;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.data.City;
import com.topface.topface.data.Options;
import com.topface.topface.data.Photo;
import com.topface.topface.requests.*;
import com.topface.topface.requests.handlers.ApiHandler;
import com.topface.topface.requests.handlers.ErrorCodes;
import com.topface.topface.ui.dialogs.TakePhotoDialog;
import com.topface.topface.ui.fragments.BaseFragment;
import com.topface.topface.ui.fragments.MenuFragment;
import com.topface.topface.ui.fragments.closing.LikesClosingFragment;
import com.topface.topface.ui.fragments.closing.MutualClosingFragment;
import com.topface.topface.ui.profile.PhotoSwitcherActivity;
import com.topface.topface.ui.settings.SettingsContainerActivity;
import com.topface.topface.utils.*;
import com.topface.topface.utils.offerwalls.Offerwalls;
import com.topface.topface.utils.social.AuthToken;
import com.topface.topface.utils.social.AuthorizationManager;

public class NavigationActivity extends BaseFragmentActivity {

    public static final String FROM_AUTH = "com.topface.topface.AUTH";

    public static final String CURRENT_FRAGMENT_ID = "NAVIGATION_FRAGMENT";
    private FragmentManager mFragmentManager;
    private MenuFragment mFragmentMenu;
    private DrawerLayout mDrawerLayout;
    private FullscreenController mFullscreenController;

    private SharedPreferences mPreferences;
    private Novice mNovice;
    private boolean needAnimate = false;
    private boolean isPopupVisible = false;
    private boolean menuEnabled = true;
    private static boolean mHasClosingsForThisSession;
    private static boolean mClosingsOnProfileUpdateInvoked = false;

    private static NavigationActivity instance = null;
    private ActionBarDrawerToggle mDrawerToggle;
    private NavigationBarController mNavBarController;

    BroadcastReceiver mCountersReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (mNavBarController != null) mNavBarController.refreshNotificators();
        }
    };

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
        setMenuEnabled(true);
        mFragmentManager = getSupportFragmentManager();

        initDrawerLayout();
        initCounterBadges();
        if (!AuthToken.getInstance().isEmpty()) {
            showFragment(savedInstanceState);
        }
    }

    private void initCounterBadges() {
        final View badges = getLayoutInflater().inflate(R.layout.layout_notifications_badges, getActionBarContainerLayout(), true);
        mNavBarController = new NavigationBarController((ViewGroup) badges);
        final View home = findViewById(android.R.id.home);
        if (home == null) return;
        home.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @SuppressWarnings("deprecation")
            @Override
            public void onGlobalLayout() {
                int diff = home.getTop() - badges.getHeight() / 7; // badges.getHeight()/6 - 1/6 бэйджа будет торчать над home иконкой
                badges.findViewById(R.id.loCounters).setPadding(home.getLeft() + home.getWidth() / 2, diff > 0 ? diff : 0, 0, 0);
                findViewById(android.R.id.home).setPadding(0, 0, Utils.getPxFromDp(10), 0);
                mNavBarController.refreshNotificators();
                if (Build.VERSION.SDK_INT >= 16) {
                    home.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    home.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
            }
        });
    }

    private ViewGroup getActionBarContainerLayout() {
        return recurActionBarContainerSearch((ViewGroup) getWindow().getDecorView());
    }

    private ViewGroup recurActionBarContainerSearch(ViewGroup viewGroup) {
        if (viewGroup.getClass().getSimpleName().equals(ActionBarContainer.class.getSimpleName())) {
            return viewGroup;
        }

        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View child = viewGroup.getChildAt(i);
            if (child instanceof ViewGroup) {
                ViewGroup result = recurActionBarContainerSearch((ViewGroup) child);
                if (result != null) return result;
            }
        }
        return null;
    }

    @Override
    protected void onCreateAsync() {
        super.onCreateAsync();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(App.getContext());
        String isGcmSupported = preferences.getString(GCMUtils.IS_GCM_SUPPORTED, null);
        if (isGcmSupported != null) {
            GCMUtils.GCM_SUPPORTED = Boolean.getBoolean(isGcmSupported);
        }

        mNovice = Novice.getInstance(getPreferences());
        mNovice.initNoviceFlags();
    }

    private void initDrawerLayout() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.loNavigationDrawer);
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.drawable.ic_drawer,  /* nav drawer icon to replace 'Up' caret */
                R.string.app_name,  /* "open drawer" description */
                R.string.app_name  /* "close drawer" description */
        ) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                mFragmentMenu.showNovice(mNovice);
//                ActionBar actionBar = getSupportActionBar();
//                actionBar.setTitle(R.string.app_name);
//                actionBar.setSubtitle(null);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                actionsAfterRegistration();
//                mFragmentMenu.getCurrentFragment().refreshActionBarTitles();
            }
        };


        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mFragmentMenu = (MenuFragment) mFragmentManager.findFragmentById(R.id.fragment_menu);
        mFragmentMenu.setClickable(true);
        mFragmentMenu.setOnFragmentSelected(new MenuFragment.OnFragmentSelectedListener() {
            @Override
            public void onFragmentSelected(int fragmentId) {
                mDrawerLayout.closeDrawer(GravityCompat.START);
            }
        });
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
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private SharedPreferences getPreferences() {
        if (mPreferences == null) {
            mPreferences = getSharedPreferences(Static.PREFERENCES_TAG_SHARED, Context.MODE_PRIVATE);
        }
        return mPreferences;
    }

    public void showFragment(int fragmentId) {
        mFragmentMenu.selectMenu(fragmentId);
    }

    private void showFragment(Bundle savedInstanceState) {
        Intent intent = getIntent();
        //Получаем id фрагмента, если он открыт
        int currentFragment = intent.getIntExtra(GCMUtils.NEXT_INTENT, -1);

        if (currentFragment == -1) {
            currentFragment = savedInstanceState != null ?
                    savedInstanceState.getInt(CURRENT_FRAGMENT_ID, BaseFragment.F_DATING) :
                    BaseFragment.F_DATING;
        }

        showFragment(currentFragment);
    }

    public void showContent() {
        mDrawerLayout.openDrawer(GravityCompat.START);
    }

    @Override
    public void onLoadProfile() {
        super.onLoadProfile();
        mFragmentMenu.onLoadProfile();
        AuthorizationManager.extendAccessToken(NavigationActivity.this);
        PopupManager manager = new PopupManager(this);
        manager.showOldVersionPopup(CacheProfile.getOptions().maxVersion);
        manager.showRatePopup();
        actionsAfterRegistration();
        if (CacheProfile.show_ad) {
            mFullscreenController = new FullscreenController(this);
            mFullscreenController.requestFullscreen();
        }
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
        int id = intent.getIntExtra(GCMUtils.NEXT_INTENT, -1);
        if (id != -1) {
            mFragmentMenu.showFragment(id);
        }
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
        if (MenuFragment.logoutInvoked) {
            mFragmentMenu.onStopClosings();
            MenuFragment.logoutInvoked = false;
        }

        if (!AuthToken.getInstance().isEmpty() &&
                !CacheProfile.premium && !mHasClosingsForThisSession &&
                mFragmentMenu.getCurrentFragmentId() != MenuFragment.F_PROFILE
                && !mFragmentMenu.isClosed() && mClosingsOnProfileUpdateInvoked) {
            if (CacheProfile.unread_likes > 0 || CacheProfile.unread_mutual > 0) {
                onClosings();
            }
        }

        if(mNavBarController != null) mNavBarController.refreshNotificators();
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(mCountersReceiver, new IntentFilter(CountersManager.UPDATE_COUNTERS));

        if (mFragmentMenu.isClosed()) {
            updateClosing();
        }
    }

    private void actionsAfterRegistration() {
        if (!AuthToken.getInstance().isEmpty()) {
            if (CacheProfile.photo == null) {
                takePhoto(new TakePhotoDialog.TakePhotoListener() {
                    @Override
                    public void onPhotoSentSuccess(final Photo photo) {
                        if (CacheProfile.photos != null) {
                            CacheProfile.photos.add(photo);
                            LocalBroadcastManager.getInstance(NavigationActivity.this).sendBroadcast(new Intent(PhotoSwitcherActivity.DEFAULT_UPDATE_PHOTOS_INTENT));
                        }
                        PhotoMainRequest request = new PhotoMainRequest(getApplicationContext());
                        request.photoid = photo.getId();
                        request.callback(new ApiHandler() {

                            @Override
                            public void success(IApiResponse response) {
                                CacheProfile.photo = photo;
                                LocalBroadcastManager.getInstance(getContext()).sendBroadcast(new Intent(ProfileRequest.PROFILE_UPDATE_ACTION));
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
                    public void onPhotoSentFailure() {
                        Toast.makeText(App.getContext(), R.string.photo_add_error, Toast.LENGTH_SHORT).show();
                        needOpenDialog = true;
                    }

                    @Override
                    public void onDialogClose() {
                        if (!CacheProfile.isEmpty() && (CacheProfile.city.isEmpty() || CacheProfile.needCityConfirmation(getApplicationContext()))
                                && !CacheProfile.wasCityAsked) {
                            CacheProfile.wasCityAsked = true;
                            CacheProfile.onCityConfirmed(getApplicationContext());
                            startActivityForResult(new Intent(getApplicationContext(), CitySearchActivity.class),
                                    CitySearchActivity.INTENT_CITY_SEARCH_AFTER_REGISTRATION);
                        }
                    }
                });
            } else if ((CacheProfile.city == null || CacheProfile.city.isEmpty()) && !CacheProfile.wasCityAsked) {
                CacheProfile.wasCityAsked = true;
                startActivityForResult(new Intent(getApplicationContext(), CitySearchActivity.class),
                        CitySearchActivity.INTENT_CITY_SEARCH_ACTIVITY);
            }
        }
    }


    @Override
    public void onCloseFragment() {
        showFragment(MenuFragment.DEFAULT_FRAGMENT);
    }

    @Override
    public boolean startAuth() {
        boolean result = super.startAuth();
        return result;
    }

    private void toggleDrawerLayout() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            mDrawerLayout.openDrawer(GravityCompat.START);
        }
    }

    public void setPopupVisible(boolean visibility) {
        isPopupVisible = visibility;
    }

    @Override
    public void onBackPressed() {
        if (mFullscreenController != null && mFullscreenController.isFullScreenBannerVisible() && !isPopupVisible) {
            mFullscreenController.hideFullscreenBanner((ViewGroup) findViewById(R.id.loBannerContainer));
        } else {
            super.onBackPressed();
            isPopupVisible = false;
        }
    }

    public void setMenuEnabled(boolean enabled) {
        menuEnabled = enabled;
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
                    final String city_name = extras.getString(CitySearchActivity.INTENT_CITY_NAME);
                    final String city_full = extras.getString(CitySearchActivity.INTENT_CITY_FULL_NAME);
                    final int city_id = extras.getInt(CitySearchActivity.INTENT_CITY_ID);
                    SettingsRequest request = new SettingsRequest(this);
                    request.cityid = city_id;
                    request.callback(new ApiHandler() {

                        @Override
                        public void success(IApiResponse response) {
                            CacheProfile.city = new City(city_id, city_name,
                                    city_full);
                            LocalBroadcastManager.getInstance(getApplicationContext())
                                    .sendBroadcast(new Intent(ProfileRequest.PROFILE_UPDATE_ACTION));
                        }

                        @Override
                        public void fail(int codeError, IApiResponse response) {
                        }
                    }).exec();
                }
            }
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(
                CURRENT_FRAGMENT_ID,
                mFragmentMenu.getCurrentFragmentId()
        );
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

    public static void selectFragment(int fragmentId) {
        Intent intent = new Intent();
        intent.setAction(MenuFragment.SELECT_MENU_ITEM);
        intent.putExtra(MenuFragment.SELECTED_FRAGMENT_ID, fragmentId);
        LocalBroadcastManager.getInstance(App.getContext()).sendBroadcast(intent);
    }

    public static void onLogout() {
        mHasClosingsForThisSession = false;
        mClosingsOnProfileUpdateInvoked = false;
        MenuFragment.onLogout();
    }

    @Override
    protected void onClosingDataReceived() {
        super.onClosingDataReceived();
        if (!CacheProfile.premium && !mClosingsOnProfileUpdateInvoked && !mHasClosingsForThisSession) {
            mClosingsOnProfileUpdateInvoked = true;
            Options.Closing closing = CacheProfile.getOptions().closing;
            if (closing.isClosingsEnabled()) {
                getIntent().putExtra(GCMUtils.NEXT_INTENT, mFragmentMenu.getCurrentFragmentId());
                Debug.log("Closing:Last fragment ID=" + mFragmentMenu.getCurrentFragmentId() + " from NavigationActivity");
                MutualClosingFragment.usersProcessed = !closing.isMutualClosingAvailable();
                LikesClosingFragment.usersProcessed = !closing.isLikesClosingAvailable();
                if (!MutualClosingFragment.usersProcessed || !LikesClosingFragment.usersProcessed) {
                    onClosings();
                }
            }
        }
    }

    public void onClosings() {
        if (CacheProfile.unread_mutual == 0) {
            MutualClosingFragment.usersProcessed = true;
        }
        if (CacheProfile.unread_likes == 0) {
            LikesClosingFragment.usersProcessed = true;
        }
        Options.Closing closing = CacheProfile.getOptions().closing;
        if (closing.enabledMutual && !MutualClosingFragment.usersProcessed) {
            mFragmentMenu.onClosings(BaseFragment.F_MUTUAL);
            showFragment(BaseFragment.F_MUTUAL);
            return;
        }
        if (closing.enabledSympathies && !LikesClosingFragment.usersProcessed) {
            mFragmentMenu.onClosings(BaseFragment.F_LIKES);
            showFragment(BaseFragment.F_LIKES);
            return;
        }
        if (!mHasClosingsForThisSession) {
            mHasClosingsForThisSession = true;
        }
        mFragmentMenu.onStopClosings();
        showFragment(null); // it will take fragment id from getIntent() extra data
    }

    private void updateClosing() {
        if (CacheProfile.premium) {
            if (CacheProfile.premium) {
                Options.Closing closing = CacheProfile.getOptions().closing;
                if (closing.isClosingsEnabled()) {
                    closing.stopForPremium();
                    onClosings();
                }
            }
        }
    }

    @Override
    protected void onProfileUpdated() {
        super.onProfileUpdated();
        updateClosing();
    }

    public static void restartNavigationActivity(int fragmentId) {
        Activity activity = instance;
        Intent intent = new Intent(activity, NavigationActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(GCMUtils.NEXT_INTENT, fragmentId);
        activity.startActivity(intent);
        activity.finish();
    }
}
