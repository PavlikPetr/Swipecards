package com.topface.topface.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.slidingmenu.lib.SlidingMenu;
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
import com.topface.topface.ui.fragments.MenuFragment;
import com.topface.topface.ui.settings.SettingsContainerActivity;
import com.topface.topface.utils.*;
import com.topface.topface.utils.offerwalls.Offerwalls;
import com.topface.topface.utils.social.AuthToken;
import com.topface.topface.utils.social.AuthorizationManager;

public class NavigationActivity extends BaseFragmentActivity implements View.OnClickListener {

    public static final String FROM_AUTH = "com.topface.topface.AUTH";

    public static final String CURRENT_FRAGMENT_ID = "NAVIGATION_FRAGMENT";
    private FragmentManager mFragmentManager;
    private MenuFragment mFragmentMenu;
    private FullscreenController mFullscreenController;

    private SharedPreferences mPreferences;
    private Novice mNovice;
    private boolean needAnimate = false;
    private SlidingMenu mSlidingMenu;
    private boolean isPopupVisible = false;
    private boolean menuEnabled;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        mNeedAnimate = false;
        super.onCreate(savedInstanceState);
        if (isNeedBroughtToFront(getIntent())) {
            // При открытии активити из лаунчера перезапускаем ее
            finish();
            return;
        }
        setMenuEnabled(true);
        Debug.log(this, "onCreate");
        mFragmentManager = getSupportFragmentManager();

        initSlidingMenu();
        if (!AuthToken.getInstance().isEmpty()) {
            showFragment(savedInstanceState);
        }
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
        try {
            Looper.prepare();
            Offerwalls.init(getApplicationContext());
            Looper.loop();
        } catch (Exception e) {
            Debug.error(e);
        }
    }

    private void initSlidingMenu() {
        mSlidingMenu = new SlidingMenu(this);
        mSlidingMenu.setMode(SlidingMenu.LEFT);
        mSlidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
        mSlidingMenu.setMenu(R.layout.fragment_side_menu);
        mSlidingMenu.attachToActivity(this, SlidingMenu.SLIDING_WINDOW);
        mSlidingMenu.setBehindOffset(Utils.getPxFromDp(60));
        mSlidingMenu.setShadowWidth(Utils.getPxFromDp(20));
        mSlidingMenu.setShadowDrawable(R.drawable.shadow);
        mSlidingMenu.setFadeEnabled(false);
        mSlidingMenu.setBehindScrollScale(0f);
        mFragmentMenu = (MenuFragment) mFragmentManager.findFragmentById(R.id.fragment_menu);
        mFragmentMenu.setOnFragmentSelected(new MenuFragment.OnFragmentSelectedListener() {
            @Override
            public void onFragmentSelected(int fragmentId) {
                mSlidingMenu.showContent();
            }
        });
        setSlidingMenuEvents();
    }

    private void setSlidingMenuEvents() {
        mSlidingMenu.setOnClosedListener(new SlidingMenu.OnClosedListener() {
            @Override
            public void onClosed() {
                mFragmentMenu.setClickable(false);
                BaseFragment currentFragment = mFragmentMenu.getCurrentFragment();
                if (currentFragment != null) {
                    currentFragment.activateActionBar(false);
                }
                actionsAfterRegistration();
            }
        });
        mSlidingMenu.setOnOpenListener(new SlidingMenu.OnOpenListener() {
            @Override
            public void onOpen() {
                mFragmentMenu.setClickable(true);
            }
        });
        mSlidingMenu.setOnOpenedListener(new SlidingMenu.OnOpenedListener() {
            @Override
            public void onOpened() {
                BaseFragment currentFragment = mFragmentMenu.getCurrentFragment();
                if (currentFragment != null) {
                    currentFragment.activateActionBar(true);
                }

                mFragmentMenu.showNovice(mNovice);
            }
        });
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
        getIntent().putExtra(GCMUtils.NEXT_INTENT, mFragmentMenu.getCurrentFragmentId());
        onClosings();
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
            LocaleConfig.changeLocale(this, App.getConfig().getLocaleConfig().getApplicationLocale(), mFragmentMenu.getCurrentFragmentId());
            return;
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
                                    Toast.makeText(
                                            NavigationActivity.this,
                                            App.getContext().getString(R.string.general_wrong_photo_upload),
                                            Toast.LENGTH_LONG
                                    ).show();
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
    public void onCloseFragment() {
        showFragment(MenuFragment.DEFAULT_FRAGMENT);
        mSlidingMenu.setSlidingEnabled(true);
    }

    @Override
    public boolean startAuth() {
        boolean result = super.startAuth();
        if (result) {
            mSlidingMenu.setSlidingEnabled(false);
        }
        return result;
    }

    /*
            *  обработчик кнопки открытия меню в заголовке фрагмента
            */
    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.leftButtonContainer) {
            mSlidingMenu.toggle();
        }
    }

    public void setPopupVisible(boolean visibility) {
        isPopupVisible = visibility;
    }

    @Override
    public void onBackPressed() {
        if (mFullscreenController != null && mFullscreenController.isFullScreenBannerVisible() && !isPopupVisible) {
            mFullscreenController.hideFullscreenBanner((ViewGroup) findViewById(R.id.loBannerContainer));
        } else if (mSlidingMenu != null && !isPopupVisible) {
            if (mSlidingMenu.isMenuShowing() || !mSlidingMenu.isSlidingEnabled() || !menuEnabled) {
                super.onBackPressed();
            } else {
                mSlidingMenu.showMenu();
            }
        } else {
            super.onBackPressed();
            isPopupVisible = false;
        }
    }

    public void setMenuEnabled(boolean enabled) {
        menuEnabled = enabled;
    }

    @Override
    public boolean onCreatePanelMenu(int featureId, Menu menu) {
        if (mSlidingMenu != null && menuEnabled) {
            mSlidingMenu.toggle();
        }
        return false;
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
        outState.putInt(
                CURRENT_FRAGMENT_ID,
                mFragmentMenu.getCurrentFragmentId()
        );
    }

    ExternalLinkExecuter.OnExternalLinkListener mListener = new ExternalLinkExecuter.OnExternalLinkListener() {
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

    public static void selectFragment(int fragmentId) {
        Intent intent = new Intent();
        intent.setAction(MenuFragment.SELECT_MENU_ITEM);
        intent.putExtra(MenuFragment.SELECTED_FRAGMENT_ID, fragmentId);
        LocalBroadcastManager.getInstance(App.getContext()).sendBroadcast(intent);
    }

    public void onClosings() {
        if (CacheProfile.unread_mutual > 0) {
            mFragmentMenu.onClosings(BaseFragment.F_MUTUAL);
            showFragment(BaseFragment.F_MUTUAL);
            return;
        }
        if (CacheProfile.unread_likes > 0) {
            mFragmentMenu.onClosings(BaseFragment.F_LIKES);
            showFragment(BaseFragment.F_LIKES);
            return;
        }
        mFragmentMenu.onStopClosings();
        showFragment(null); // it will take fragment id from getIntent() extra data
    }
}
