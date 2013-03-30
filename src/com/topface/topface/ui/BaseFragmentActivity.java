package com.topface.topface.ui;

import android.R;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.WindowManager;
import com.topface.topface.ReAuthReceiver;
import com.topface.topface.Static;
import com.topface.topface.requests.ApiRequest;
import com.topface.topface.ui.analytics.TrackedFragmentActivity;
import com.topface.topface.ui.dialogs.TakePhotoDialog;
import com.topface.topface.ui.fragments.AuthFragment;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.http.IRequestClient;
import com.topface.topface.utils.social.AuthToken;

import java.util.LinkedList;

public class BaseFragmentActivity extends TrackedFragmentActivity implements IRequestClient {

    public static final String INTENT_PREV_ENTITY = "prev_entity";
    public static final String AUTH_TAG = "AUTH";

    private boolean afterOnSavedInstanceState = false;
    protected boolean needOpenDialog = true;

    private LinkedList<ApiRequest> mRequests = new LinkedList<ApiRequest>();
    private BroadcastReceiver mReauthReceiver;
    protected boolean mNeedAnimate = true;
    private boolean needAuth = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFormat(PixelFormat.RGBA_8888);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DITHER);
        if (isNeedAuth() && (AuthToken.getInstance().isEmpty() || !CacheProfile.isLoaded())) {
            startAuth();
        }
        if (mNeedAnimate) {
            overridePendingTransition(com.topface.topface.R.anim.slide_in_from_right, com.topface.topface.R.anim.slide_out_left);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
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

        if(!afterOnSavedInstanceState) {
            afterOnSavedInstanceState = true;
            registerReceiver(mReauthReceiver, new IntentFilter(ReAuthReceiver.REAUTH_INTENT));
        }
    }

    public void startAuth() {
        Fragment authFragment = getSupportFragmentManager().findFragmentByTag(AUTH_TAG);
        if (authFragment == null || !authFragment.isAdded()) {
            if (authFragment == null) {
                authFragment = AuthFragment.newInstance();
            }
            getSupportFragmentManager().beginTransaction().add(R.id.content, authFragment, AUTH_TAG).commit();
            needAuth = false;
        }
    }

    public void close(Fragment fragment) {
        getSupportFragmentManager().beginTransaction().remove(fragment).commit();
        onInit();
    }

    public void onInit() {
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (!afterOnSavedInstanceState) {
            unregisterReceiver(mReauthReceiver);
            afterOnSavedInstanceState = true;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        removeAllRequests();
        if (!afterOnSavedInstanceState) {
            unregisterReceiver(mReauthReceiver);
            afterOnSavedInstanceState = true;
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
        return intent != null && (intent.getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0;
    }


    protected boolean isNeedAuth() {
        return needAuth;
    }

    protected void takePhoto(TakePhotoDialog.TakePhotoListener listener) {
        if (needOpenDialog) {
            TakePhotoDialog newFragment = TakePhotoDialog.newInstance();
            newFragment.setOnTakePhotoListener(listener);
            newFragment.show(getSupportFragmentManager(), TakePhotoDialog.TAG);
            needOpenDialog = false;
        }
    }
}