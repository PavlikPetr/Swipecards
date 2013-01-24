package com.topface.topface.ui;

import android.R;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import com.google.analytics.tracking.android.EasyTracker;
import com.topface.topface.ReAuthReceiver;
import com.topface.topface.Static;
import com.topface.topface.requests.ApiRequest;
import com.topface.topface.ui.analytics.TrackedFragmentActivity;
import com.topface.topface.ui.fragments.AuthFragment;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.http.IRequestClient;
import com.topface.topface.utils.social.AuthToken;

import java.util.LinkedList;

public class BaseFragmentActivity extends TrackedFragmentActivity implements IRequestClient {

    public static final String INTENT_PREV_ENTITY = "prev_entity";
    public static final String AUTH_TAG = "AUTH";

    private LinkedList<ApiRequest> mRequests = new LinkedList<ApiRequest>();
    private BroadcastReceiver mReauthReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (new AuthToken(getApplicationContext()).isEmpty() || !CacheProfile.isLoaded()) {
            startAuth();
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
                startAuth();
            }
        };
        registerReceiver(mReauthReceiver, new IntentFilter(ReAuthReceiver.REAUTH_INTENT));
    }


    public void startAuth() {
        AuthFragment af = AuthFragment.newInstance();
        getSupportFragmentManager().beginTransaction().add(R.id.content, af, AUTH_TAG).commit();
    }

    public void close(Fragment fragment) {
        getSupportFragmentManager().beginTransaction().remove(fragment).commit();
        onInit();
    }

    public void onInit() {
    }

    @Override
    public void onStart() {
        super.onStart();
        EasyTracker.getInstance().activityStart(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        removeAllRequests();
        unregisterReceiver(mReauthReceiver);
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
    public void onStop() {
        super.onStop();
        EasyTracker.getInstance().activityStop(this);
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
}