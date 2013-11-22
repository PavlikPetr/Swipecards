package com.topface.topface.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;

import com.topface.topface.GCMUtils;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.data.Options;
import com.topface.topface.requests.ApiRequest;
import com.topface.topface.requests.ProfileRequest;
import com.topface.topface.ui.analytics.TrackedFragmentActivity;
import com.topface.topface.ui.dialogs.TakePhotoDialog;
import com.topface.topface.ui.fragments.AuthFragment;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.LocaleConfig;
import com.topface.topface.utils.http.IRequestClient;
import com.topface.topface.utils.social.AuthToken;

import java.util.LinkedList;

public class BaseFragmentActivity extends TrackedFragmentActivity implements IRequestClient {

    public static final String INTENT_PREV_ENTITY = "prev_entity";
    public static final String AUTH_TAG = "AUTH";

    protected boolean needOpenDialog = true;

    private LinkedList<ApiRequest> mRequests = new LinkedList<ApiRequest>();
    private BroadcastReceiver mReauthReceiver;
    protected boolean mNeedAnimate = true;
    private BroadcastReceiver mProfileLoadReceiver;
    private boolean afterOnSaveInstanceState;

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
    public void onStart() {
        super.onStart();
        //Странный глюк на некоторых устройствах (воспроизводится например на HTC One V),
        // из-за которого показывается лоадер в ActionBar
        // этот метод можно использовать только после setContent
        setSupportProgressBarIndeterminateVisibility(false);
    }

    @Override
    public void setSupportProgressBarIndeterminateVisibility(boolean visible) {
        if (getSupportActionBar() != null) {
            super.setSupportProgressBarIndeterminateVisibility(visible);
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
        supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        // для корректного отображения картинок
        getWindow().setFormat(PixelFormat.RGBA_8888);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DITHER);
        // добавляем анимацию открытия нового activity
        if (mNeedAnimate) {
            overridePendingTransition(com.topface.topface.R.anim.slide_in_from_right, com.topface.topface.R.anim.slide_out_left);
        }
    }

    private void checkProfileLoad() {
        if (CacheProfile.isLoaded()) {
            if (!CacheProfile.isEmpty() && !AuthToken.getInstance().isEmpty()) {
                onLoadProfile();
            } else {
                startAuth();
            }

        } else if (mProfileLoadReceiver == null) {
            mProfileLoadReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    //Уведомлять о загрузке профиля следует только если мы авторизованы
                    if (!CacheProfile.isEmpty() && !AuthToken.getInstance().isEmpty()) {
                        checkProfileLoad();
                    }
                }
            };

            try {
                LocalBroadcastManager.getInstance(this).registerReceiver(
                        mProfileLoadReceiver,
                        new IntentFilter(CacheProfile.ACTION_PROFILE_LOAD)
                );
            } catch (Exception ex) {
                Debug.error(ex);
            }
        }
    }

    protected void onLoadProfile() {
        if (CacheProfile.isEmpty() || AuthToken.getInstance().isEmpty()) {
            startAuth();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        afterOnSaveInstanceState = false;
        checkProfileLoad();
        registerReauthReceiver();
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(mProfileUpdateReceiver, new IntentFilter(ProfileRequest.PROFILE_UPDATE_ACTION));
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
            getSupportFragmentManager().beginTransaction().replace(getContentViewCompat(), authFragment, AUTH_TAG).commit();
            return true;
        }
        return false;
    }

    public void startFragment(Fragment fragment) {
        if (!afterOnSaveInstanceState) {
            getSupportFragmentManager().beginTransaction().add(getContentViewCompat(), fragment).addToBackStack(null).commit();
        }
    }

    public static int getContentViewCompat() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH ?
                android.R.id.content : R.id.action_bar_activity_content;
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
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        afterOnSaveInstanceState = true;
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

    protected boolean isNeedBroughtToFront(Intent intent) {
        return intent != null &&
                !intent.getBooleanExtra(GCMUtils.NOTIFICATION_INTENT, false) &&
                (intent.getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0;
    }


    protected boolean isNeedAuth() {
        return true;
    }

    protected void takePhoto(TakePhotoDialog.TakePhotoListener listener) {
        if (needOpenDialog) {
            if (this instanceof NavigationActivity) {
                ((NavigationActivity)this).setTakePhotoDialogStarted(true);
            }
            TakePhotoDialog newFragment = TakePhotoDialog.newInstance();
            newFragment.setOnTakePhotoListener(listener);
            try {
                newFragment.show(getSupportFragmentManager(), TakePhotoDialog.TAG);
            } catch (Exception e) {
                Debug.error(e);
            }
            needOpenDialog = false;
        }
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
}