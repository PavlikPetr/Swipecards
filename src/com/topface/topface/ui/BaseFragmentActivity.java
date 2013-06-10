package com.topface.topface.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.WindowManager;
import com.topface.topface.GCMUtils;
import com.topface.topface.Static;
import com.topface.topface.requests.ApiRequest;
import com.topface.topface.ui.analytics.TrackedFragmentActivity;
import com.topface.topface.ui.dialogs.ConfirmEmailDialog;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LocaleConfig.updateConfiguration(getBaseContext());
        setWindowOptions();

        (new Thread() {
            @Override
            public void run() {
                super.run();
                inBackgroundThread();
            }
        }).start();
    }

    @SuppressWarnings("deprecation")
    private void setWindowOptions() {
        getWindow().setFormat(PixelFormat.RGBA_8888);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DITHER);
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
                    checkProfileLoad();
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
        checkProfileLoad();
        registerReauthReceiver();
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
            getSupportFragmentManager().beginTransaction().add(android.R.id.content, authFragment, AUTH_TAG).commit();
            return true;
        }
        return false;
    }

    public void startFragment(Fragment fragment) {
        if (!afterOnSaveInstanceState) {
            getSupportFragmentManager().beginTransaction().add(android.R.id.content, fragment).addToBackStack(null).commit();
        }
    }

    public void close(Fragment fragment) {
        close(fragment, false);
    }

    public void close(Fragment fragment, boolean needFireEvent) {
        getSupportFragmentManager().beginTransaction().remove(fragment).commit();
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
        request.cancel();
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
                !intent.getBooleanExtra(GCMUtils.GCM_INTENT, false) &&
                (intent.getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0;
    }


    protected boolean isNeedAuth() {
        return true;
    }

    protected void takePhoto(TakePhotoDialog.TakePhotoListener listener) {
        if (needOpenDialog) {
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

    protected void showConfirmEmailDialog() {
        ConfirmEmailDialog newFragment = ConfirmEmailDialog.newInstance();
        try {
            newFragment.show(getSupportFragmentManager(), ConfirmEmailDialog.TAG);
        } catch (Exception e) {
            Debug.error(e);
        }
    }

    protected void inBackgroundThread() {
    }
}