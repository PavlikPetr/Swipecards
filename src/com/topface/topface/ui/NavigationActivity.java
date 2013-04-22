package com.topface.topface.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.Toast;
import com.topface.billing.BillingUtils;
import com.topface.topface.App;
import com.topface.topface.GCMUtils;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.data.City;
import com.topface.topface.data.Photo;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.PhotoMainRequest;
import com.topface.topface.requests.ProfileRequest;
import com.topface.topface.requests.SettingsRequest;
import com.topface.topface.requests.handlers.ApiHandler;
import com.topface.topface.ui.dialogs.TakePhotoDialog;
import com.topface.topface.ui.fragments.BaseFragment;
import com.topface.topface.ui.fragments.DatingFragment;
import com.topface.topface.ui.fragments.FragmentSwitchController;
import com.topface.topface.ui.fragments.FragmentSwitchController.FragmentSwitchListener;
import com.topface.topface.ui.fragments.MenuFragment;
import com.topface.topface.ui.fragments.MenuFragment.FragmentMenuListener;
import com.topface.topface.ui.settings.SettingsContainerActivity;
import com.topface.topface.ui.views.NoviceLayout;
import com.topface.topface.utils.*;
import com.topface.topface.utils.GeoUtils.GeoLocationManager;
import com.topface.topface.utils.GeoUtils.GeoPreferencesManager;
import com.topface.topface.utils.offerwalls.Offerwalls;
import com.topface.topface.utils.social.AuthToken;
import com.topface.topface.utils.social.AuthorizationManager;

public class NavigationActivity extends BaseFragmentActivity implements View.OnClickListener {

    public static final String FROM_AUTH = "com.topface.topface.AUTH";

    public static final String CURRENT_FRAGMENT_ID = "NAVIGATION_FRAGMENT";
    private FragmentManager mFragmentManager;
    private MenuFragment mFragmentMenu;
    private FragmentSwitchController mFragmentSwitcher;
    private FullscreenController mFullscreenController;

    private SharedPreferences mPreferences;
    private NoviceLayout mNoviceLayout;
    private Novice mNovice;
    private boolean needAnimate = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        mNeedAnimate = false;
        super.onCreate(savedInstanceState);

        if (isNeedBroughtToFront(getIntent())) {
            // При открытии активити из лаунчера перезапускаем ее
            finish();
            return;
        }
        setContentView(R.layout.ac_navigation);

        Debug.log(this, "onCreate");
        mFragmentManager = getSupportFragmentManager();

        initFragmentSwitcher();
        if (!AuthToken.getInstance().isEmpty()) {
            showFragment(savedInstanceState);
        }

        mNoviceLayout = (NoviceLayout) findViewById(R.id.loNovice);
    }

    @Override
    protected void inBackgroundThread() {
        super.inBackgroundThread();
        mNovice = Novice.getInstance(getPreferences());
        Looper.prepare();
        Offerwalls.init(getApplicationContext());
        Looper.loop();
    }

    private SharedPreferences getPreferences() {
        if (mPreferences == null) {
            mPreferences = getSharedPreferences(Static.PREFERENCES_TAG_SHARED, Context.MODE_PRIVATE);
        }
        return mPreferences;
    }

    private void initFragmentSwitcher() {
        mFragmentSwitcher = (FragmentSwitchController) findViewById(R.id.fragment_switcher);
        mFragmentSwitcher.setFragmentSwitchListener(mFragmentSwitchListener);
        mFragmentSwitcher.setFragmentManager(mFragmentManager);

        mFragmentMenu = (MenuFragment) mFragmentManager.findFragmentById(R.id.fragment_menu);
        mFragmentMenu = (MenuFragment) mFragmentManager.findFragmentById(R.id.fragment_menu);
        mFragmentMenu.setOnMenuListener(mOnFragmentMenuListener);
    }

    private void showFragment(int fragmentId) {
        mFragmentSwitcher.showFragment(fragmentId);
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

    @Override
    public void onLoadProfile() {
        super.onLoadProfile();
        mFragmentMenu.onLoadProfile();
        AuthorizationManager.extendAccessToken(NavigationActivity.this);
        PopupManager manager = new PopupManager(this);
        manager.showOldVersionPopup(CacheProfile.getOptions().max_version);
        manager.showRatePopup();
        actionsAfterRegistration();

        if (CacheProfile.show_ad) {
            mFullscreenController = new FullscreenController(this);
            mFullscreenController.requestFullscreen();
        }
        sendLocation();
    }

    private void sendLocation() {
        GeoLocationManager locationManager = new GeoLocationManager(App.getContext());
        Location curLocation = locationManager.getLastKnownLocation();

        GeoPreferencesManager preferencesManager = new GeoPreferencesManager(App.getContext());
        preferencesManager.saveLocation(curLocation);

        SettingsRequest settingsRequest = new SettingsRequest(this);
        settingsRequest.location = curLocation;
        settingsRequest.exec();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        int id = intent.getIntExtra(GCMUtils.NEXT_INTENT, -1);
        if (id != -1) {
            mFragmentSwitcher.showFragmentWithAnimation(id);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        //Отправляем не обработанные запросы на покупку
        BillingUtils.sendQueueItems();

        if (needAnimate) {
            overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_right);
        }
        needAnimate = true;

        //Если перешли в приложение по ссылке, то этот класс смотрит что за ссылка и делает то что нужно
        new ExternalLinkExecuter(listener).execute(getIntent());

        App.checkProfileUpdate();
    }

    private void actionsAfterRegistration() {
        if (!AuthToken.getInstance().isEmpty()) {
            if (CacheProfile.photo == null) {
                takePhoto(new TakePhotoDialog.TakePhotoListener() {
                    @Override
                    public void onPhotoSentSuccess(final Photo photo) {
                        if (CacheProfile.photos != null) {
                            CacheProfile.photos.add(photo);
                        }
                        PhotoMainRequest request = new PhotoMainRequest(getApplicationContext());
                        request.photoid = photo.getId();
                        request.callback(new ApiHandler() {

                            @Override
                            public void success(ApiResponse response) {
                                CacheProfile.photo = photo;
                                LocalBroadcastManager.getInstance(getContext()).sendBroadcast(new Intent(ProfileRequest.PROFILE_UPDATE_ACTION));
                            }

                            @Override
                            public void fail(int codeError, ApiResponse response) {
                                if (codeError == ApiResponse.NON_EXIST_PHOTO_ERROR) {
                                    if (CacheProfile.photos != null && CacheProfile.photos.contains(photo)) {
                                        CacheProfile.photos.remove(photo);
                                    }
                                    Toast.makeText(NavigationActivity.this, App.getContext().getString(R.string.general_wrong_photo_upload), 2000);
                                }
                            }

                            @Override
                            public void always(ApiResponse response) {
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
    public void close(Fragment fragment, boolean needInit) {
        super.close(fragment, needInit);
        showFragment(FragmentSwitchController.DEFAULT_FRAGMENT);
    }

    /*
        *  обработчик кнопки открытия меню в заголовке фрагмента
        */
    @Override
    public void onClick(View view) {
        if (view.getId() != R.id.btnNavigationHome)
            return;
        if (mFragmentSwitcher.getAnimationState() == FragmentSwitchController.EXPAND) {
            mFragmentSwitcher.closeMenu();
        } else {
            mFragmentSwitcher.openMenu();
        }
    }

    @Override
    public void onBackPressed() {
        if (mFullscreenController != null && mFullscreenController.isFullScreenBannerVisible()) {
            mFullscreenController.hideFullscreenBanner((ViewGroup) findViewById(R.id.loBannerContainer));
        } else if (mFragmentSwitcher != null) {
            if (mFragmentSwitcher.getAnimationState() == FragmentSwitchController.EXPAND) {
                super.onBackPressed();
            } else {
                mFragmentMenu.refreshNotifications();
                mFragmentSwitcher.openMenu();
            }
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreatePanelMenu(int featureId, Menu menu) {
        if (mFragmentSwitcher != null) {
            if (mFragmentSwitcher.getAnimationState() != FragmentSwitchController.EXPAND) {
                if (mFragmentMenu != null) {
                    mFragmentMenu.refreshNotifications();
                }
                mFragmentSwitcher.openMenu();
            } else {
                mFragmentSwitcher.closeMenu();
            }
        }
        return false;
    }

    private FragmentMenuListener mOnFragmentMenuListener = new FragmentMenuListener() {
        @Override
        public void onMenuClick(int fragmentId) {
            mFragmentSwitcher.showFragmentWithAnimation(fragmentId);
        }
    };

    public void onDialogCancel() {
        Fragment fragment = mFragmentManager.findFragmentById(R.id.fragment_container);
        if (fragment instanceof DatingFragment) {
            DatingFragment datingFragment = (DatingFragment) fragment;
            datingFragment.onDialogCancel();
        }
    }

    private FragmentSwitchListener mFragmentSwitchListener = new FragmentSwitchListener() {
        @Override
        public void beforeExpanding() {
            mFragmentMenu.setClickable(true);
            mFragmentMenu.show();
            mFragmentMenu.refreshNotifications();
        }

        @Override
        public void afterClosing() {
            mFragmentMenu.setClickable(false);
            mFragmentMenu.hide();
            if (mFragmentSwitcher.getCurrentFragment() != null) {
                mFragmentSwitcher.getCurrentFragment().activateActionBar(false);
            }
            actionsAfterRegistration();
        }

        @Override
        public void afterOpening() {
            if (mFragmentSwitcher.getCurrentFragment() != null) {
                mFragmentSwitcher.getCurrentFragment().activateActionBar(true);
            }

            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (mNovice != null && mNoviceLayout != null && mFragmentMenu != null) {
                        if (mNovice.isMenuCompleted()) return;

                        if (mNovice.isShowFillProfile()) {
                            mNoviceLayout.setLayoutRes(R.layout.novice_fill_profile, mFragmentMenu.getProfileButtonOnClickListener());
                            AlphaAnimation alphaAnimation = new AlphaAnimation(0.0F, 1.0F);
                            alphaAnimation.setDuration(400L);
                            mNoviceLayout.startAnimation(alphaAnimation);
                            mNovice.completeShowFillProfile();
                        }
                    }
                }
            });
        }

    };

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        super.startActivityForResult(intent, requestCode);

    }

    @Override
    protected void onDestroy() {
        //В некоторых редких случаях выпадает NullPointerException при destroyDrawingCache,
        //поэтому на всякий случай оборачиваем в try
        try {
            super.onDestroy();
            unbindDrawables(findViewById(R.id.NavigationLayout));
            System.gc();
        } catch (Exception e) {
            Debug.error(e);
        }

        //Для запроса фото при следующем создании NavigationActivity
        if (CacheProfile.photo == null) CacheProfile.wasAvatarAsked = false;
    }

    private void unbindDrawables(View view) {
        if (view != null) {
            if (view.getBackground() != null) {
                view.getBackground().setCallback(null);
            }
            if (view instanceof ViewGroup) {
                for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                    unbindDrawables(((ViewGroup) view).getChildAt(i));
                }
                ((ViewGroup) view).removeAllViews();
            }
        }
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
                        public void success(ApiResponse response) {
                            CacheProfile.city = new City(city_id, city_name,
                                    city_full);
                            LocalBroadcastManager.getInstance(getApplicationContext())
                                    .sendBroadcast(new Intent(ProfileRequest.PROFILE_UPDATE_ACTION));
                        }

                        @Override
                        public void fail(int codeError, ApiResponse response) {
                        }
                    }).exec();
                }
            }
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        int currentFragmentId = mFragmentSwitcher.getCurrentFragmentId();
        outState.putInt(
                CURRENT_FRAGMENT_ID,
                currentFragmentId == -1 ?
                        FragmentSwitchController.DEFAULT_FRAGMENT :
                        currentFragmentId
        );
    }

    ExternalLinkExecuter.OnExternalLinkListener listener = new ExternalLinkExecuter.OnExternalLinkListener() {
        @Override
        public void onProfileLink(int profileID) {
            ContainerActivity.getProfileIntent(profileID, NavigationActivity.this);
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
}
