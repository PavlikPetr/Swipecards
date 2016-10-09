package com.topface.topface.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.topface.framework.utils.Debug;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.data.Options;
import com.topface.topface.requests.ApiRequest;
import com.topface.topface.statistics.NotificationStatistics;
import com.topface.topface.ui.analytics.TrackedFragmentActivity;
import com.topface.topface.ui.fragments.AuthFragment;
import com.topface.topface.ui.views.toolbar.NavigationToolbarViewModel;
import com.topface.topface.ui.views.toolbar.ToolbarBaseViewModel;
import com.topface.topface.ui.views.toolbar.ToolbarSettingsData;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.GoogleMarketApiManager;
import com.topface.topface.utils.IActivityDelegate;
import com.topface.topface.utils.LocaleConfig;
import com.topface.topface.utils.Utils;
import com.topface.topface.utils.actionbar.ActionBarView;
import com.topface.topface.utils.gcmutils.GCMUtils;
import com.topface.topface.utils.http.IRequestClient;
import com.topface.topface.utils.social.AuthToken;

import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.Locale;

public abstract class BaseFragmentActivity extends TrackedFragmentActivity implements IRequestClient {

    public static final String AUTH_TAG = "AUTH";
    public static final String GOOGLE_AUTH_STARTED = "google_auth_started";
    public static final String IGNORE_NOTIFICATION_INTENT = "IGNORE_NOTIFICATION_INTENT";
    private static final String APP_START_LABEL_FORM = "gcm_%d_%s";
    public ActionBarView actionBarView;
    private boolean mIndeterminateSupported = false;
    private LinkedList<ApiRequest> mRequests = new LinkedList<>();
    private boolean mNeedAnimate = true;
    private BroadcastReceiver mProfileLoadReceiver;
    private Toolbar mToolbar;
    private ToolbarBaseViewModel mToolbarBaseViewModel;
    private BroadcastReceiver mProfileUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            onProfileUpdated();
        }
    };
    private BroadcastReceiver mOptionsUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            onOptionsUpdated();
        }
    };
    private BroadcastReceiver mReauthReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (isNeedAuth()) {
                try {
                    startAuth();
                } catch (Exception e) {
                    Debug.error(e);
                }
            }
        }
    };
    private boolean mRunning;
    private boolean mGoogleAuthStarted;
    private boolean mHasContent = true;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // игнорируем нажатие аппаратной кнопки
        return keyCode == KeyEvent.KEYCODE_MENU || super.onKeyDown(keyCode, event);
    }

    @SuppressWarnings("unused")
    public IActivityDelegate getActivityDelegate() {
        return this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setWindowOptions();
        if (mHasContent) {
            setContentView(getContentLayout());
        }
        Intent intent = getIntent();
        if (intent.getBooleanExtra(GCMUtils.NOTIFICATION_INTENT, false)) {
            App.setStartLabel(String.format(Locale.getDefault(), APP_START_LABEL_FORM,
                    intent.getIntExtra(GCMUtils.GCM_TYPE, -1),
                    intent.getStringExtra(GCMUtils.GCM_LABEL)));
        }
        LocaleConfig.updateConfiguration(getBaseContext());
        initActionBarOptions(getSupportActionBar());
    }

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        super.setContentView(layoutResID);
        getToolbar();
    }

    protected Toolbar getToolbar() {
        if (mToolbar == null) {
            mToolbar = (Toolbar) findViewById(R.id.toolbar);
            if (mToolbar != null) {
                setSupportActionBar(mToolbar);
            }
        }
        return mToolbar;
    }

    protected ToolbarBaseViewModel generateToolbarViewModel() {
        return new ToolbarBaseViewModel(getToolbar());
    }

    protected ToolbarBaseViewModel getToolbarViewModel() {
        if (mToolbarBaseViewModel == null) {
            mToolbarBaseViewModel = generateToolbarViewModel();
        }

        return mToolbarBaseViewModel;
    }

    public void setToolbarSettings(@NotNull ToolbarSettingsData settings) {
        ToolbarBaseViewModel toolbarBaseViewModel = getToolbarViewModel();
        if (toolbarBaseViewModel != null) {
            if (settings.getTitle() != null) {
                toolbarBaseViewModel.setTitle(settings.getTitle());
            }
            if (settings.getSubtitle() != null) {
                toolbarBaseViewModel.setSubtitle(settings.getSubtitle());
            }
            if (settings.getIcon() != null) {
                toolbarBaseViewModel.setUpButton(settings.getIcon());
            }
        }
    }

    public void setToolBarVisibility(boolean isVisible) {
        Toolbar bar = getToolbar();
        if (bar != null) {
            bar.setVisibility(isVisible ? View.VISIBLE : View.GONE);
        }
    }

    protected abstract int getContentLayout();

    @Override
    protected void onRestoreInstanceState(@NotNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mGoogleAuthStarted = savedInstanceState.getBoolean(GOOGLE_AUTH_STARTED);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mGoogleAuthStarted) {
            outState.putBoolean(GOOGLE_AUTH_STARTED, true);
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setWindowContentOverlayCompat();
    }

    @Override
    public void onStart() {
        super.onStart();
        mRunning = true;
        //Странный глюк на некоторых устройствах (воспроизводится например на HTC One V),
        // из-за которого показывается лоадер в ActionBar
        // этот метод можно использовать только после setContent
        setSupportProgressBarIndeterminateVisibility(false);
    }

    @Override
    public void onStop() {
        super.onStop();
        mRunning = false;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void setSupportProgressBarIndeterminateVisibility(boolean visible) {
        if (mIndeterminateSupported) {
            if (getSupportActionBar() != null) {
                super.setSupportProgressBarIndeterminateVisibility(visible);
            }
        }
    }

    protected void initActionBarOptions(ActionBar actionBar) {
//        actionBar.setDisplayHomeAsUpEnabled(true);
//        actionBar.setDisplayShowTitleEnabled(true);
    }

    protected void setActionBarView() {
        actionBarView.setArrowUpView((String) getTitle());
    }

    /**
     * Установка флагов Window
     */
    @SuppressWarnings("deprecation")
    private void setWindowOptions() {
        Window window = getWindow();
        // supportRequestWindowFeature() вызывать только до setContent(),
        // метод setSupportProgressBarIndeterminateVisibility(boolean) вызывать строго после setContent();
        mIndeterminateSupported = supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        // для корректного отображения картинок
        window.setFormat(PixelFormat.RGBA_8888);
        window.addFlags(WindowManager.LayoutParams.FLAG_DITHER);
        // добавляем анимацию открытия нового activity
        // иногда еще надо совсем _не_ анимировать, поэтому флаг оставлен
        if (!mNeedAnimate) {
            overridePendingTransition(0, 0);
        }
        // status bar color
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }

    protected void setNeedTransitionAnimation(boolean needAnimate) {
        mNeedAnimate = needAnimate;
    }

    private void checkProfileLoad() {
        if (CacheProfile.isLoaded()) {
            if (isLoggedIn()) {
                onLoadProfile();
            } else {
                registerLoadProfileReceiver();
                startAuth();
            }
        } else {
            registerLoadProfileReceiver();
        }
    }

    protected boolean isLoggedIn() {
        return !CacheProfile.isEmpty() && !AuthToken.getInstance().isEmpty();
    }

    private void registerLoadProfileReceiver() {
        if (mProfileLoadReceiver == null) {
            mProfileLoadReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    //Уведомлять о загрузке профиля следует только если мы авторизованы
                    if (!CacheProfile.isEmpty() && !AuthToken.getInstance().isEmpty() && isActivityRestoredState()) {
                        checkProfileLoad();
                    }
                }
            };
            LocalBroadcastManager.getInstance(this)
                    .registerReceiver(mProfileLoadReceiver, new IntentFilter(CacheProfile.ACTION_PROFILE_LOAD));
        }
    }

    protected void onLoadProfile() {
        Debug.log("onLoadProfile in " + ((Object) this).getClass().getSimpleName());
        if (CacheProfile.isEmpty() || AuthToken.getInstance().isEmpty() && isActivityRestoredState()) {
            startAuth();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (GoogleMarketApiManager.isGoogleAccountExists() && mGoogleAuthStarted) {
            App.mOpenIabHelperManager.freeHelper();
            App.mOpenIabHelperManager.init(App.getContext());
            mGoogleAuthStarted = false;
        }
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(mReauthReceiver, new IntentFilter(AuthFragment.REAUTH_INTENT));
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(mProfileUpdateReceiver, new IntentFilter(CacheProfile.PROFILE_UPDATE_ACTION));
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(mOptionsUpdateReceiver, new IntentFilter(Options.OPTIONS_RECEIVED_ACTION));

        /*
        Sending notification open event to statistics. Only done once when activity started from notification.
        Then IGNORE_NOTIFICATION_INTENT prevents from repeated sending.
         */
        Intent intent = getIntent();
        if (!intent.getBooleanExtra(IGNORE_NOTIFICATION_INTENT, false) &&
                intent.getBooleanExtra(GCMUtils.NOTIFICATION_INTENT, false)) {
            NotificationStatistics.sendOpened(intent.getIntExtra(GCMUtils.GCM_TYPE, -1),
                    intent.getStringExtra(GCMUtils.GCM_LABEL));
            intent.putExtra(IGNORE_NOTIFICATION_INTENT, true);
            setIntent(intent);
        }
    }

    @Override
    protected void onResumeFragments() {
        /*
        checkProfileLoad() may commit fragment transaction adding or replasing AuthFragment.
        That is why it shouldn't be called in onResume() to avoid state loss.
        See http://www.androiddesignpatterns.com/2013/08/fragment-transaction-commit-state-loss.html,
        http://developer.android.com/reference/android/support/v4/app/FragmentActivity.html#onResume%28%29 and
        http://stackoverflow.com/questions/16265733/failure-delivering-result-onactivityforresult
         */
        super.onResumeFragments();
        if (AuthToken.getInstance().isEmpty()) {
            startAuth();
        }
        checkProfileLoad();
        //restart -> open NavigationActivity
        if (App.getLocaleConfig().fetchToSystemLocale()) {
            LocaleConfig.changeLocale(this, App.getLocaleConfig().getApplicationLocale());
            return;
        }
        LocaleConfig.localeChangeInitiated = false;
    }

    public boolean startAuth() {
        Fragment authFragment = getSupportFragmentManager().findFragmentByTag(AUTH_TAG);
        if (isNeedAuth() && (authFragment == null || !authFragment.isAdded())) {
            if (authFragment == null) {
                authFragment = AuthFragment.newInstance();
            }
            getSupportFragmentManager().beginTransaction().replace(android.R.id.content, authFragment, AUTH_TAG).commit();
            return true;
        }
        return false;
    }

    @SuppressWarnings("unused")
    public void startFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .add(android.R.id.content, fragment)
                .addToBackStack(null)
                .commitAllowingStateLoss();
    }

    public void close(Fragment fragment) {
        close(fragment, false);
    }

    public void close(Fragment fragment, boolean needFireEvent) {
        getSupportFragmentManager().beginTransaction().remove(fragment).commitAllowingStateLoss();
        if (needFireEvent) {
            onCloseFragment();
        }
    }

    protected void onCloseFragment() {

    }

    @Override
    protected void onPause() {
        super.onPause();
        removeAllRequests();
        if (mProfileLoadReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mProfileLoadReceiver);
            mProfileLoadReceiver = null;
        }
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mProfileUpdateReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mOptionsUpdateReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReauthReceiver);
    }

    private void removeAllRequests() {
        if (mRequests != null && mRequests.size() > 0) {
            for (ApiRequest request : mRequests) {
                cancelRequest(request);
            }
            mRequests.clear();
        }
    }

    @Override
    public void registerRequest(ApiRequest request) {
        if (!mRequests.contains(request)) {
            mRequests.add(request);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GoogleMarketApiManager.GOOGLE_AUTH_CODE) {
            mGoogleAuthStarted = true;
        }

        //Вот такая херня сделана для того, чтобы result фэйсбуковского приложение обрабатывал
        //AuthFragment. Потому что фб приложение обязательно должно стартовать из активити
        //и ответ возвращать тоже в активити.
        Fragment authFragment = getSupportFragmentManager().findFragmentByTag(AUTH_TAG);
        if (authFragment != null) {
            authFragment.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void cancelRequest(ApiRequest request) {
        request.cancelFromUi();
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        if (requestCode != -1) {
            intent.putExtra(App.INTENT_REQUEST_KEY, requestCode);
        }
        if (Utils.isIntentAvailable(this, intent)) {
            super.startActivityForResult(intent, requestCode);
        }
    }

    @Override
    public void finish() {
        super.finish();
        if (!mNeedAnimate) {
            overridePendingTransition(0, 0);
        }
    }

    protected boolean isNeedBroughtToFront(Intent intent) {
        return intent != null &&
                !intent.getBooleanExtra(GCMUtils.NOTIFICATION_INTENT, false) &&
                (intent.getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0;
    }


    protected boolean isNeedAuth() {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (onPreFinish()) {
                    finish();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public boolean doPreFinish() {
        return onPreFinish();
    }

    protected boolean onPreFinish() {
        return true;
    }

    protected void onProfileUpdated() {
    }

    protected void onOptionsUpdated() {
    }

    @SuppressWarnings("UnusedDeclaration")
    public boolean isPackageInstalled(String packagename, Context context) {
        PackageManager pm = context.getPackageManager();
        try {
            if (pm != null) {
                pm.getPackageInfo(packagename, PackageManager.GET_ACTIVITIES);
                return true;
            } else {
                return false;
            }
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    /**
     * Set the window content overlay on device's that don't respect the theme
     * attribute.
     */
    @SuppressWarnings("deprecation")
    private void setWindowContentOverlayCompat() {
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.JELLY_BEAN_MR2) {
            // Get the content view
            View contentView = findViewById(android.R.id.content);

            // Make sure it's a valid instance of a FrameLayout
            if (contentView instanceof FrameLayout) {
                TypedValue tv = new TypedValue();

                // Get the windowContentOverlay value of the current theme
                if (getTheme().resolveAttribute(
                        android.R.attr.windowContentOverlay, tv, true)) {

                    // If it's a valid resource, set it as the foreground drawable
                    // for the content view
                    if (tv.resourceId != 0) {
                        FrameLayout layout = ((FrameLayout) contentView);
                        layout.setForeground(
                                getResources().getDrawable(tv.resourceId));
                        layout.setForegroundGravity(GravityCompat.RELATIVE_HORIZONTAL_GRAVITY_MASK);
                    }
                }
            }
        }
    }

    public void onUpClick() {
        if (doPreFinish()) {
            if (!onSupportNavigateUp()) {
                finish();
            }
        }
    }

    @Override
    public boolean supportShouldUpRecreateTask(Intent targetIntent) {
        return super.supportShouldUpRecreateTask(targetIntent) && isTaskRoot();
    }

    @Override
    public void supportNavigateUpTo(Intent upIntent) {
        if (!isTaskRoot()) {
            finish();
        } else {
            upIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(upIntent);
            finish();
        }
    }

    @SuppressWarnings("unused")
    public boolean isRunning() {
        return mRunning;
    }

    protected void setHasContent(boolean value) {
        mHasContent = value;
    }
}
