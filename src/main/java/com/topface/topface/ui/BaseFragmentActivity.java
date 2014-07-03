package com.topface.topface.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBar;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.topface.framework.utils.Debug;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.requests.ApiRequest;
import com.topface.topface.ui.analytics.TrackedFragmentActivity;
import com.topface.topface.ui.fragments.AuthFragment;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.LocaleConfig;
import com.topface.topface.utils.controllers.StartActionsController;
import com.topface.topface.utils.gcmutils.GCMUtils;
import com.topface.topface.utils.http.IRequestClient;
import com.topface.topface.utils.social.AuthToken;
import com.topface.topface.utils.social.AuthorizationManager;

import java.util.LinkedList;

public class BaseFragmentActivity extends TrackedFragmentActivity implements IRequestClient {

    public static final String INTENT_PREV_ENTITY = "prev_entity";
    public static final String AUTH_TAG = "AUTH";

    private boolean mIndeterminateSupported = false;

    private LinkedList<ApiRequest> mRequests = new LinkedList<>();
    private BroadcastReceiver mReauthReceiver;
    private boolean mNeedAnimate = true;
    private BroadcastReceiver mProfileLoadReceiver;
    private StartActionsController mStartActionsController;

    private BroadcastReceiver mProfileUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            onProfileUpdated();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LocaleConfig.updateConfiguration(getBaseContext());
        setWindowOptions();
        initActionBar(getSupportActionBar());
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setWindowContentOverlayCompat();
        mStartActionsController = new StartActionsController(this);
        onRegisterStartActions(mStartActionsController);
    }

    @Override
    public void onStart() {
        super.onStart();
        //Странный глюк на некоторых устройствах (воспроизводится например на HTC One V),
        // из-за которого показывается лоадер в ActionBar
        // этот метод можно использовать только после setContent
        setSupportProgressBarIndeterminateVisibility(false);
    }

    @Override
    public void setSupportProgressBarIndeterminateVisibility(boolean visible) {
        if (mIndeterminateSupported) {
            if (getSupportActionBar() != null) {
                super.setSupportProgressBarIndeterminateVisibility(visible);
            }
        }
    }

    /**
     * Выставляем опции для ActionBar
     */
    protected void initActionBar(ActionBar actionBar) {
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setIcon(R.drawable.ic_home_topface_white);
        }
    }

    /**
     * Установка флагов Window
     */
    @SuppressWarnings("deprecation")
    private void setWindowOptions() {
        // supportRequestWindowFeature() вызывать только до setContent(),
        // метод setSupportProgressBarIndeterminateVisibility(boolean) вызывать строго после setContent();
        mIndeterminateSupported = supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        // для корректного отображения картинок
        getWindow().setFormat(PixelFormat.RGBA_8888);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DITHER);
        // добавляем анимацию открытия нового activity
        if (mNeedAnimate) {
            overridePendingTransition(com.topface.topface.R.anim.slide_in_from_right, com.topface.topface.R.anim.slide_out_left);
        }
    }

    protected void setNeedTransitionAnimation(boolean needAnimate) {
        mNeedAnimate = needAnimate;
    }

    private void checkProfileLoad() {
        if (CacheProfile.isLoaded()) {
            if (!CacheProfile.isEmpty() && !AuthToken.getInstance().isEmpty()) {
                onLoadProfile();
            } else {
                if (mProfileLoadReceiver == null) {
                    mProfileLoadReceiver = new BroadcastReceiver() {
                        @Override
                        public void onReceive(Context context, Intent intent) {
                            //Уведомлять о загрузке профиля следует только если мы авторизованы
                            if (!CacheProfile.isEmpty() && !AuthToken.getInstance().isEmpty()) {
                                checkProfileLoad();
                            }
                        }
                    };
                    LocalBroadcastManager.getInstance(this).registerReceiver(
                            mProfileLoadReceiver,
                            new IntentFilter(CacheProfile.ACTION_PROFILE_LOAD)
                    );
                }
                startAuth();
            }
        }
    }

    protected void onLoadProfile() {
        AuthorizationManager.extendAccessToken(this);
        if (CacheProfile.isEmpty() || AuthToken.getInstance().isEmpty()) {
            startAuth();
        } else {
            mStartActionsController.onProcessAction();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkProfileLoad();
        registerReauthReceiver();
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(mProfileUpdateReceiver, new IntentFilter(CacheProfile.PROFILE_UPDATE_ACTION));
    }

    private void registerReauthReceiver() {
        //Если при запросе вернулась ошибка что нет токена, кидается соответствующий интент.
        //здесь он ловится, и открывается фрагмент авторизации
        mReauthReceiver = new BroadcastReceiver() {
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

        try {
            registerReceiver(mReauthReceiver, new IntentFilter(AuthFragment.REAUTH_INTENT));
        } catch (Exception ex) {
            Debug.error(ex);
        }
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
        try {
            unregisterReceiver(mReauthReceiver);
            if (mProfileLoadReceiver != null) {
                LocalBroadcastManager.getInstance(this).unregisterReceiver(mProfileLoadReceiver);
                mProfileLoadReceiver = null;
            }
        } catch (Exception ex) {
            Debug.error(ex);
        }
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mProfileUpdateReceiver);
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
            intent.putExtra(Static.INTENT_REQUEST_KEY, requestCode);
        }
        super.startActivityForResult(intent, requestCode);
    }

    @Override
    public void finish() {
        super.finish();
        if (mNeedAnimate) {
            overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_right);
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

    protected Integer getOptionsMenuRes() {
        return null;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onPreFinish();
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected void onPreFinish() {
    }

    protected void onProfileUpdated() {
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
     * Method for overload where you can register start actions
     * User startActionController argument to register actions
     * Note: actions can be placed here for global usage in all child activities
     */
    protected void onRegisterStartActions(StartActionsController startActionsController) {
    }

    /**
     * Set the window content overlay on device's that don't respect the theme
     * attribute.
     */
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
}