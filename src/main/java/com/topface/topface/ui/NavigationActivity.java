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
import com.topface.topface.data.Options;
import com.topface.topface.data.Photo;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.PhotoMainRequest;
import com.topface.topface.requests.ProfileRequest;
import com.topface.topface.requests.SettingsRequest;
import com.topface.topface.requests.handlers.ApiHandler;
import com.topface.topface.requests.handlers.ErrorCodes;
import com.topface.topface.ui.dialogs.TakePhotoDialog;
import com.topface.topface.ui.fragments.BaseFragment;
import com.topface.topface.ui.fragments.DatingFragment;
import com.topface.topface.ui.fragments.MenuFragment;
import com.topface.topface.ui.fragments.closing.LikesClosingFragment;
import com.topface.topface.ui.fragments.closing.MutualClosingFragment;
import com.topface.topface.ui.profile.PhotoSwitcherActivity;
import com.topface.topface.ui.settings.SettingsContainerActivity;
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
import com.topface.topface.utils.offerwalls.Offerwalls;
import com.topface.topface.utils.social.AuthToken;
import com.topface.topface.utils.social.AuthorizationManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class NavigationActivity extends CustomTitlesBaseFragmentActivity {

    public static final String FROM_AUTH = "com.topface.topface.AUTH";

    public static final String CURRENT_FRAGMENT_ID = "NAVIGATION_FRAGMENT";
    private FragmentManager mFragmentManager;
    private MenuFragment mMenuFragment;
    private DrawerLayout mDrawerLayout;
    private FullscreenController mFullscreenController;

    private SharedPreferences mPreferences;
    private Novice mNovice;
    private boolean needAnimate = false;
    private boolean isPopupVisible = false;
    private static boolean mHasClosingsForThisSession = false;
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
        setMenuEnabled(true);
        mFragmentManager = getSupportFragmentManager();

        initDrawerLayout();

        new BackgroundThread() {
            @Override
            public void execute() {
                onCreateAsync();
            }
        };
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
        mNovice = Novice.getInstance(getPreferences());
        mNovice.initNoviceFlags();
    }

    private void initDrawerLayout() {
        mMenuFragment = (MenuFragment) mFragmentManager.findFragmentById(R.id.fragment_menu);
        if (mMenuFragment == null) {
            mMenuFragment = new MenuFragment();
        }
        mMenuFragment.setClickable(true);
        mMenuFragment.setOnFragmentSelected(new MenuFragment.OnFragmentSelectedListener() {
            @Override
            public void onFragmentSelected(int fragmentId) {
                mDrawerLayout.closeDrawer(GravityCompat.START);
            }
        });
        if (!mMenuFragment.isAdded()) {
            mFragmentManager
                    .beginTransaction()
                    .add(R.id.fragment_menu, mMenuFragment)
                    .commit();
        }

        mDrawerLayout = (DrawerLayout) findViewById(R.id.loNavigationDrawer);
        mDrawerLayout.setScrimColor(Color.argb(217, 0, 0, 0));
        mDrawerLayout.setDrawerShadow(R.drawable.shadow_left_menu_right, GravityCompat.START);
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
                mMenuFragment.showNovice(mNovice);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                actionsAfterRegistration();
            }
        };
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
        return mDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    private SharedPreferences getPreferences() {
        if (mPreferences == null) {
            mPreferences = getSharedPreferences(Static.PREFERENCES_TAG_SHARED, Context.MODE_PRIVATE);
        }
        return mPreferences;
    }

    public void showFragment(int fragmentId) {
        mMenuFragment.selectMenu(fragmentId);
    }

    private int getSavedFragmentId(Bundle savedInstanceState) {
        Intent intent = getIntent();
        //Получаем id фрагмента, если он открыт
        int currentFragment = intent.getIntExtra(GCMUtils.NEXT_INTENT, -1);

        if (currentFragment == -1) {
            currentFragment = savedInstanceState != null ?
                    savedInstanceState.getInt(CURRENT_FRAGMENT_ID, BaseFragment.F_DATING) :
                    BaseFragment.F_DATING;
        }

        return currentFragment;
    }

    public void hideContent() {
        mDrawerLayout.closeDrawer(GravityCompat.START);
    }

    @Override
    public void onLoadProfile() {
        super.onLoadProfile();
        mMenuFragment.onLoadProfile();
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
            mMenuFragment.switchFragment(id);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMenuFragment.onLoadProfile();
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
            mMenuFragment.onStopClosings();
            MenuFragment.logoutInvoked = false;
        }

        if (!AuthToken.getInstance().isEmpty()
                && !CacheProfile.premium
                && !mHasClosingsForThisSession
                && mMenuFragment.getCurrentFragmentId() != MenuFragment.F_PROFILE
                && !mMenuFragment.isClosed()
                && mClosingsOnProfileUpdateInvoked) {
            if (CacheProfile.unread_likes > 0 || CacheProfile.unread_mutual > 0) {
                onClosings();
            }
        }

        if (mNavBarController != null) {
            mNavBarController.refreshNotificators();
        }
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(mCountersReceiver, new IntentFilter(CountersManager.UPDATE_COUNTERS));

        if (mMenuFragment.isClosed()) {
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
                            Intent intent = new Intent(PhotoSwitcherActivity.DEFAULT_UPDATE_PHOTOS_INTENT);
                            intent.putExtra(PhotoSwitcherActivity.INTENT_CLEAR, true);
                            intent.putExtra(PhotoSwitcherActivity.INTENT_PHOTOS, CacheProfile.photos);
                            LocalBroadcastManager.getInstance(NavigationActivity.this).sendBroadcast(intent);
                        } else {
                            Intent intent = new Intent(PhotoSwitcherActivity.DEFAULT_UPDATE_PHOTOS_INTENT);
                            ArrayList<Photo> photos = new ArrayList<Photo>();
                            photos.add(photo);
                            intent.putParcelableArrayListExtra(PhotoSwitcherActivity.INTENT_PHOTOS, photos);
//                            LocalBroadcastManager.getInstance(NavigationActivity.this).sendBroadcast(intent);
                        }
                        takePhotoDialogStarted = false;
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
                        takePhotoDialogStarted = false;
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
        if (mDrawerLayout != null) {
            mDrawerLayout.setEnabled(enabled);
        }
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
                        final City city = new City(new JSONObject(extras.getString(CitySearchActivity.INTENT_CITY)));
                        SettingsRequest request = new SettingsRequest(this);
                        request.cityid = city.id;
                        request.callback(new ApiHandler() {

                            @Override
                            public void success(IApiResponse response) {
                                CacheProfile.city = city;
                                LocalBroadcastManager.getInstance(getApplicationContext())
                                        .sendBroadcast(new Intent(ProfileRequest.PROFILE_UPDATE_ACTION));
                            }

                            @Override
                            public void fail(int codeError, IApiResponse response) {
                            }
                        }).exec();
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
                getIntent().putExtra(GCMUtils.NEXT_INTENT, mMenuFragment.getCurrentFragmentId());
                Debug.log("Closing:Last fragment ID=" + mMenuFragment.getCurrentFragmentId() + " from NavigationActivity");
                MutualClosingFragment.usersProcessed = !closing.isMutualClosingAvailable();
                LikesClosingFragment.usersProcessed = !closing.isLikesClosingAvailable();
                if (!MutualClosingFragment.usersProcessed || !LikesClosingFragment.usersProcessed) {
                    onClosings();
                } else {
                    LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(DatingFragment.CLOSINGS_FILTER));
                }
            } else {
                LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(DatingFragment.CLOSINGS_FILTER));
            }
        } else {
            MutualClosingFragment.usersProcessed = true;
            LikesClosingFragment.usersProcessed = true;
        }
    }

    public void onClosings() {
        if (mHasClosingsForThisSession) return;
        if (CacheProfile.unread_mutual == 0) {
            MutualClosingFragment.usersProcessed = true;
        }
        if (CacheProfile.unread_likes == 0) {
            LikesClosingFragment.usersProcessed = true;
        }
        if (!mClosingsOnProfileUpdateInvoked) return;

        Options.Closing closing = CacheProfile.getOptions().closing;
        if (closing.enabledMutual && !MutualClosingFragment.usersProcessed) {
            mMenuFragment.onClosings(BaseFragment.F_MUTUAL);
            showFragment(BaseFragment.F_MUTUAL);
            return;
        }
        if (closing.enabledSympathies && !LikesClosingFragment.usersProcessed) {
            mMenuFragment.onClosings(BaseFragment.F_LIKES);
            showFragment(BaseFragment.F_LIKES);
            return;
        }
        if (!mHasClosingsForThisSession) {
            mHasClosingsForThisSession = true;
        }
        mMenuFragment.onStopClosings();
        getSavedFragmentId(null); // it will take fragment id from getIntent() extra data
        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(DatingFragment.CLOSINGS_FILTER));
    }

    private void updateClosing() {
        if (CacheProfile.premium) {
            Options.Closing closing = CacheProfile.getOptions().closing;
            if (closing.isClosingsEnabled()) {
                closing.stopForPremium();
                mClosingsOnProfileUpdateInvoked = true;
                onClosings();
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
