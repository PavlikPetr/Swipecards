package com.topface.topface.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.*;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;
import com.topface.billing.BillingUtils;
import com.topface.topface.App;
import com.topface.topface.GCMUtils;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.data.*;
import com.topface.topface.requests.*;
import com.topface.topface.requests.handlers.ApiHandler;
import com.topface.topface.requests.handlers.BaseApiHandler;
import com.topface.topface.ui.dialogs.TakePhotoDialog;
import com.topface.topface.ui.fragments.*;
import com.topface.topface.ui.fragments.FragmentSwitchController.FragmentSwitchListener;
import com.topface.topface.ui.fragments.MenuFragment.FragmentMenuListener;
import com.topface.topface.ui.settings.SettingsContainerActivity;
import com.topface.topface.ui.views.ImageViewRemote;
import com.topface.topface.ui.views.NoviceLayout;
import com.topface.topface.utils.*;
import com.topface.topface.utils.social.AuthToken;
import com.topface.topface.utils.social.AuthorizationManager;
import ru.ideast.adwired.AWView;
import ru.ideast.adwired.events.OnNoBannerListener;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NavigationActivity extends BaseFragmentActivity implements View.OnClickListener {

    public static final String RATING_POPUP = "RATING_POPUP";
    public static final String FROM_AUTH = "com.topface.topface.AUTH";
    public static final int RATE_POPUP_TIMEOUT = 86400000; // 1000 * 60 * 60 * 24 * 1 (1 сутки)
    public static final int UPDATE_INTERVAL = 10 * 60 * 1000;
    private FragmentManager mFragmentManager;
    private MenuFragment mFragmentMenu;
    private FragmentSwitchController mFragmentSwitcher;

    private SharedPreferences mPreferences;
    private NoviceLayout mNoviceLayout;
    private Novice mNovice;
    private boolean needAnimate = false;

    private BroadcastReceiver mServerResponseReceiver;

    private boolean isNeedAuth = true;

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

        if (CacheProfile.isLoaded()) {
            onInit();
        } else {
            isNeedAuth = false;
        }

        setStopTime();
        mNovice = Novice.getInstance(getPreferences());
        mNoviceLayout = (NoviceLayout) findViewById(R.id.loNovice);
    }

    private SharedPreferences getPreferences() {
        if (mPreferences == null) {
            mPreferences = getSharedPreferences(Static.PREFERENCES_TAG_SHARED, Context.MODE_PRIVATE);
        }
        return mPreferences;
    }

    private void requestFullscreen() {
        if(CacheProfile.isLoaded()) {
            Options.Page startPage = CacheProfile.getOptions().pages.get(Options.PAGE_START);
            if (startPage != null){
                if (startPage.floatType.equals(Options.FLOAT_TYPE_BANNER)) {
                    if (startPage.banner.equals(Options.BANNER_ADWIRED)) {
                        requestAdwiredFullscreen();
                    } else if (startPage.banner.equals(Options.BANNER_TOPFACE)){
                        requestTopfaceFullscreen();
                    }
                }
            }
        }
    }

    private void requestAdwiredFullscreen() {
        try {
            if (CacheProfile.isLoaded()) {
//            && !CacheProfile.paid) {
//                Locale ukraineLocale = new Locale("uk", "UA", "");
//                if (Locale.getDefault().equals(ukraineLocale)) {
                    AWView adwiredView = (AWView) getLayoutInflater().inflate(R.layout.banner_adwired, null);
                    ((ViewGroup) findViewById(R.id.loBannerContainer)).addView(adwiredView);
                    adwiredView.setVisibility(View.VISIBLE);
                    adwiredView.setOnNoBannerListener(new OnNoBannerListener() {
                        @Override
                        public void onNoBanner() {
                            requestTopfaceFullscreen();
                        }
                    });
                    adwiredView.request('0');
//                }
            }
        } catch (Exception ex) {
            Debug.error(ex);
        }
    }

    private boolean showFullscreenBanner(String url) {
        long currentTime = System.currentTimeMillis();
        long lastCall = getPreferences().getLong(Static.PREFERENCES_LAST_FULLSCREEN_TIME, currentTime);
        boolean passByTime = !getPreferences().contains(Static.PREFERENCES_LAST_FULLSCREEN_TIME)
                || Math.abs(currentTime-lastCall) > DateUtils.DAY_IN_MILLISECONDS;
        boolean passByUrl = passFullScreenByUrl(url);

        return passByUrl && passByTime;
    }

    private boolean passFullScreenByUrl(String url) {
        Set<String> urlSet = getPreferences().getStringSet(Static.PREFERENCES_FULLSCREEN_URLS_SET, new HashSet<String>());
        return !urlSet.contains(url);
    }

    private void addLastFullsreenShowedTime() {
        SharedPreferences.Editor editor = getPreferences().edit();
        editor.putLong(Static.PREFERENCES_LAST_FULLSCREEN_TIME, System.currentTimeMillis());
        editor.commit();
    }

    private void addNewUrlToFullscreenSet(String url) {
        Set<String> urlSet = getPreferences().getStringSet(Static.PREFERENCES_FULLSCREEN_URLS_SET, new HashSet<String>());
        urlSet.add(url);
        SharedPreferences.Editor editor = getPreferences().edit();
        editor.putStringSet(Static.PREFERENCES_FULLSCREEN_URLS_SET,urlSet);
        editor.commit();
    }

    private void requestTopfaceFullscreen() {
        BannerRequest request = new BannerRequest(getApplicationContext());
        request.place = Options.PAGE_START;
        request.callback(new BaseApiHandler(){
            @Override
            public void success(ApiResponse response) {
                final Banner banner = Banner.parse(response);

                if (banner.action.equals(Banner.ACTION_URL)) {
                    if (showFullscreenBanner(banner.parameter)) {
                        addLastFullsreenShowedTime();
                        final View fullscreenViewGroup = getLayoutInflater().inflate(R.layout.fullscreen_topface, null);
                        final ViewGroup bannerContainer = (ViewGroup) findViewById(R.id.loBannerContainer);
                        bannerContainer.addView(fullscreenViewGroup);
                        final ImageViewRemote fullscreenImage = (ImageViewRemote)fullscreenViewGroup.findViewById(R.id.ivFullScreen);
                        fullscreenImage.setRemoteSrc(banner.url);
                        fullscreenImage.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                addNewUrlToFullscreenSet(banner.parameter);
                                hideFullscreenBanner(bannerContainer);
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(banner.parameter));
                                startActivity(intent);
                            }
                        });

                        fullscreenViewGroup.findViewById(R.id.btnClose).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                hideFullscreenBanner(bannerContainer);
                            }
                        });
                    }
                }
            }
        }).exec();
    }

    private void hideFullscreenBanner(final ViewGroup bannerContainer) {
        Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), android.R.anim.fade_out);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                bannerContainer.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        bannerContainer.startAnimation(animation);
    }

    private void initFragmentSwitcher() {
        mFragmentSwitcher = (FragmentSwitchController) findViewById(R.id.fragment_switcher);
        mFragmentSwitcher.setFragmentSwitchListener(mFragmentSwitchListener);
        mFragmentSwitcher.setFragmentManager(mFragmentManager);

        mFragmentMenu = (MenuFragment) mFragmentManager.findFragmentById(R.id.fragment_menu);
        mFragmentMenu = (MenuFragment) mFragmentManager.findFragmentById(R.id.fragment_menu);
        mFragmentMenu.setOnMenuListener(mOnFragmentMenuListener);
    }

    @Override
    public void onInit() {
        Offerwalls.init(getApplicationContext());

        Intent intent = getIntent();
        isNeedAuth = true;
        int id = intent.getIntExtra(GCMUtils.NEXT_INTENT, -1);
        if (id != -1) {
            mFragmentSwitcher.showFragment(id);

        } else {
            mFragmentSwitcher.showFragment(BaseFragment.F_DATING);
            mFragmentMenu.selectDefaultMenu();
        }
        AuthorizationManager.extendAccessToken(NavigationActivity.this);
        checkVersion(CacheProfile.getOptions().max_version);
        actionsAfterRegistration();
        requestFullscreen();
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
    protected boolean isNeedAuth() {
        return isNeedAuth;
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkProfileUpdate();

        //Отправляем не обработанные запросы на покупку
        BillingUtils.sendQueueItems();

        mServerResponseReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

            }
        };

        LocalBroadcastManager.getInstance(this).registerReceiver(mServerResponseReceiver, new IntentFilter(OptionsRequest.VERSION_INTENT));

        if (needAnimate) {
            overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_right);
        }
        needAnimate = true;
        Intent intent = getIntent();
        if(intent != null) {
            mFragmentSwitcher.showFragment(intent.getIntExtra(GCMUtils.NEXT_INTENT, BaseFragment.F_DATING));
        }
        //TODO костыль для ChatFragment, после перехода на фрагмент - выпилить
        if (mDelayedFragment != null) {
            onExtraFragment(mDelayedFragment);
            mDelayedFragment = null;
            mChatInvoke = true;
        }

        //Если перешли в приложение по ссылке, то этот класс смотрит что за ссылка и делает то что нужно
        new ExternalLinkExecuter(listener).execute(getIntent());

        requestBalance();
    }

    private void requestBalance() {
        if (CacheProfile.isLoaded()) {
            ProfileRequest request = new ProfileRequest(this);
            request.part = ProfileRequest.P_BALANCE_COUNTERS;
            request.callback(new DataApiHandler<Profile>() {

                @Override
                protected void success(Profile data, ApiResponse response) {
                    CacheProfile.likes = data.likes;
                    CacheProfile.money = data.money;
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(new Intent(ProfileRequest.PROFILE_UPDATE_ACTION));
                }

                @Override
                protected Profile parseResponse(ApiResponse response) {
                    return Profile.parse(response);
                }

                @Override
                public void fail(int codeError, ApiResponse response) {

                }
            }).exec();
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
                                    Toast.makeText(NavigationActivity.this, "Ваша фотография не соответствует правилам. Попробуйте сделать другую", 2000);
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
                        if (CacheProfile.isLoaded() && (CacheProfile.city.isEmpty() || CacheProfile.needCityConfirmation(getApplicationContext()))
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


    private void checkProfileUpdate() {
        long startTime = Calendar.getInstance().getTimeInMillis();
        long stopTime = getPreferences().getLong(Static.PREFERENCES_STOP_TIME, -1);
        if (stopTime != -1) {
            if (startTime - stopTime > UPDATE_INTERVAL) {
                App.sendProfileRequest();
            }
        }
    }

    private void checkVersion(String version) {
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String curVersion = pInfo.versionName;
            if (!TextUtils.isEmpty(version) && TextUtils.isEmpty(curVersion)) {
                String[] splittedVersion = version.split("\\.");
                String[] splittedCurVersion = curVersion.split("\\.");
                for (int i = 0; i < splittedVersion.length; i++) {
                    if (i < splittedCurVersion.length) {
                        if (Long.parseLong(splittedCurVersion[i]) < Long.parseLong(splittedVersion[i])) {
                            showOldVersionPopup();
                            return;
                        }
                    }
                }
                if (splittedCurVersion.length < splittedVersion.length) {
                    showOldVersionPopup();
                } else {
                    if (App.isOnline()) {
                        ratingPopup();
                    }
                }
            } else {
                if (App.isOnline()) {
                    ratingPopup();
                }
            }
        } catch (Exception e) {
            Debug.error("Check Version Error: " + version, e);

        }
    }

    private void showOldVersionPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setPositiveButton(R.string.popup_version_update, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Utils.goToMarket(NavigationActivity.this);

            }
        });
        builder.setNegativeButton(R.string.popup_version_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        builder.setMessage(R.string.general_version_not_supported);
        builder.create().show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        setStopTime();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mServerResponseReceiver);
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
        if (findViewById(R.id.loBannerContainer).getVisibility() != View.GONE) {
            hideFullscreenBanner((ViewGroup) findViewById(R.id.loBannerContainer));
        } else if (mFragmentSwitcher != null) {
            if (mFragmentSwitcher.getAnimationState() == FragmentSwitchController.EXPAND) {
                super.onBackPressed();
            } else {
                if (mFragmentSwitcher.isExtraFrameShown()) {
                    //TODO костыль для ChatFragment, после перехода на фрагмент - выпилить
                    //начало костыля--------------
                    if (mChatInvoke) {
                        if (mFragmentSwitcher.getCurrentExtraFragment() instanceof ProfileFragment) {
                            ((ProfileFragment) mFragmentSwitcher.getCurrentExtraFragment()).openChat();
                            mChatInvoke = false;
                        }
                        //конец костыля--------------
                    } else {
                        mFragmentSwitcher.closeExtraFragment();
                    }
                } else {
                    mFragmentMenu.refreshNotifications();
                    mFragmentSwitcher.openMenu();
                }
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
        public void onMenuClick(int buttonId) {
            int fragmentId;
            switch (buttonId) {
                case R.id.btnFragmentProfile:
                    fragmentId = BaseFragment.F_PROFILE;
                    break;
                case R.id.btnFragmentDating:
                    fragmentId = BaseFragment.F_DATING;
                    break;
                case R.id.btnFragmentLikes:
                    fragmentId = BaseFragment.F_LIKES;
                    break;
                case R.id.btnFragmentMutual:
                    fragmentId = BaseFragment.F_MUTUAL;
                    break;
                case R.id.btnFragmentDialogs:
                    fragmentId = BaseFragment.F_DIALOGS;
                    break;
                case R.id.btnFragmentTops:
                    fragmentId = BaseFragment.F_TOPS;
                    break;
                case R.id.btnFragmentVisitors:
                    fragmentId = BaseFragment.F_VISITORS;
                    break;
                case R.id.btnFragmentSettings:
                    fragmentId = BaseFragment.F_SETTINGS;
                    break;
                default:
                    fragmentId = BaseFragment.F_PROFILE;
                    break;
            }
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
            if (mNovice.isMenuCompleted()) return;

            if (mNovice.isShowFillProfile()) {
                mNoviceLayout.setLayoutRes(R.layout.novice_fill_profile, mFragmentMenu.getProfileButtonOnClickListener());
                AlphaAnimation alphaAnimation = new AlphaAnimation(0.0F, 1.0F);
                alphaAnimation.setDuration(400L);
                mNoviceLayout.startAnimation(alphaAnimation);
                mNovice.completeShowFillProfile();
            }
        }

        @Override
        public void onExtraFrameOpen() {
            mFragmentMenu.unselectAllButtons();
        }
    };


    /**
     * Попап с предложение оценить предложение
     */
    private void ratingPopup() {
        final SharedPreferences preferences = getSharedPreferences(Static.PREFERENCES_TAG_SHARED, Context.MODE_PRIVATE);

        long date_start = preferences.getLong(RATING_POPUP, 1);
        long date_now = new java.util.Date().getTime();

        if (date_start == 0 || (date_now - date_start < RATE_POPUP_TIMEOUT)) {
            return;
        } else if (date_start == 1) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putLong("RATING_POPUP", new java.util.Date().getTime());
            editor.commit();
            return;
        }

        final Dialog ratingPopup = new Dialog(this) {
            @Override
            public void onBackPressed() {
                SharedPreferences.Editor editor = preferences.edit();
                editor.putLong(RATING_POPUP, new java.util.Date().getTime());
                editor.commit();
                super.onBackPressed();
            }
        };
        ratingPopup.setTitle(R.string.dashbrd_popup_title);
        ratingPopup.setContentView(R.layout.popup_rating);
        ratingPopup.show();

        ratingPopup.findViewById(R.id.btnRatingPopupRate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.goToMarket(NavigationActivity.this);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putLong(RATING_POPUP, 0);
                editor.commit();
                ratingPopup.cancel();
            }
        });
        ratingPopup.findViewById(R.id.btnRatingPopupLate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = preferences.edit();
                editor.putLong(RATING_POPUP, new java.util.Date().getTime());
                editor.commit();
                ratingPopup.cancel();
            }
        });
        ratingPopup.findViewById(R.id.btnRatingPopupCancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = preferences.edit();
                editor.putLong(RATING_POPUP, 0);
                editor.commit();
                ratingPopup.cancel();
            }
        });
    }

    private void setStopTime() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                getPreferences().edit().putLong(Static.PREFERENCES_STOP_TIME, System.currentTimeMillis()).commit();
            }
        }).start();
    }

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
    public void close(Fragment fragment) {
        super.close(fragment);
        mFragmentSwitcher.showFragment(BaseFragment.F_DATING);
        mFragmentMenu.selectDefaultMenu();
    }

    @Override
    public boolean isTrackable() {
        return false;
    }

    public void onExtraFragment(final Fragment fragment) {
        mFragmentSwitcher.switchExtraFragment(fragment);
    }

    //TODO костыль для ChatFragment, после перехода на фрагмент - выпилить
    private Fragment mDelayedFragment;
    private boolean mChatInvoke = false;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == ContainerActivity.INTENT_CHAT_FRAGMENT) {
                if (data != null) {
                    int user_id = data.getExtras().getInt(ChatFragment.INTENT_USER_ID);
                    mDelayedFragment = ProfileFragment.newInstance(user_id, ProfileFragment.TYPE_USER_PROFILE);
                }
            } else if (requestCode == CitySearchActivity.INTENT_CITY_SEARCH_AFTER_REGISTRATION ||
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

    ExternalLinkExecuter.OnExternalLinkListener listener = new ExternalLinkExecuter.OnExternalLinkListener() {
        @Override
        public void onProfileLink(int profileID) {
            int profileType = profileID == CacheProfile.uid ? ProfileFragment.TYPE_MY_PROFILE : ProfileFragment.TYPE_USER_PROFILE;
            onExtraFragment(ProfileFragment.newInstance(profileID, profileType));
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
        }
    };
}
