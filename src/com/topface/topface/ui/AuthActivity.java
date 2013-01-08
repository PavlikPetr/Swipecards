package com.topface.topface.ui;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;
import com.google.analytics.tracking.android.EasyTracker;
import com.topface.topface.*;
import com.topface.topface.data.Auth;
import com.topface.topface.data.Options;
import com.topface.topface.data.Profile;
import com.topface.topface.receivers.ConnectionChangeReceiver;
import com.topface.topface.requests.*;
import com.topface.topface.ui.edit.EditProfileActivity;
import com.topface.topface.ui.views.RetryView;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.http.ConnectionManager;
import com.topface.topface.utils.social.AuthToken;
import com.topface.topface.utils.social.AuthorizationManager;

import java.util.List;

public class AuthActivity extends BaseFragmentActivity implements View.OnClickListener {
    private Button mFBButton;
    private Button mVKButton;
    private RetryView mRetryView;
    private ProgressBar mProgressBar;
    private AuthorizationManager mAuthorizationManager;
    private static boolean mIsAuthStart = false;

    private boolean mFromAuthorizationReceiver;
    private boolean mIsAuthorized = false;

    private BroadcastReceiver mReceiver;

    public static AuthActivity mThis;
    private ProfileRequest mProfileRequest;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Debug.log(this, "+onCreate");
        setContentView(R.layout.ac_auth);

        mRetryView = new RetryView(getApplicationContext());
        mRetryView.setErrorMsg(getString(R.string.general_data_error));
        mRetryView.addButton(RetryView.REFRESH_TEMPLATE + getString(R.string.general_dialog_retry), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                auth(new AuthToken(getApplicationContext()));
                mRetryView.setVisibility(View.GONE);
                mProgressBar.setVisibility(View.VISIBLE);
            }
        });
        mRetryView.setVisibility(View.GONE);
        RelativeLayout authContainer = (RelativeLayout) findViewById(R.id.authContainer);
        authContainer.addView(mRetryView);
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int mConnectionType = intent.getIntExtra(ConnectionChangeReceiver.CONNECTION_TYPE, -1);
                reAuthAfterInternetConnected(mConnectionType);
            }
        };
        IntentFilter filterReauthBan = new IntentFilter();
        filterReauthBan.addAction(ConnectionChangeReceiver.REAUTH);
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, filterReauthBan);

        mAuthorizationManager = AuthorizationManager.getInstance(this);
        mAuthorizationManager.setOnAuthorizationHandler(new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case AuthorizationManager.AUTHORIZATION_FAILED:
                        authorizationFailed(ApiResponse.NETWORK_CONNECT_ERROR);
                        break;
                    case AuthorizationManager.DIALOG_COMPLETED:
                        hideButtons();
                        break;
                    case AuthorizationManager.TOKEN_RECEIVED:
                        auth((AuthToken) msg.obj);
                        break;
                    case AuthorizationManager.AUTHORIZATION_CANCELLED:
                        showButtons();
                        break;
                    default:
                        super.handleMessage(msg);
                }
            }
        });

        // Facebook button
        mFBButton = (Button) findViewById(R.id.btnAuthFB);
        mFBButton.setOnClickListener(this);

        // Vkontakte button
        mVKButton = (Button) findViewById(R.id.btnAuthVK);
        mVKButton.setOnClickListener(this);

        // Progress
        mProgressBar = (ProgressBar) findViewById(R.id.prsAuthLoading);

        checkOnline();
    }

    private void checkOnline() {
        if (!App.isOnline()) {
            Toast.makeText(this, getString(R.string.general_internet_off), Toast.LENGTH_SHORT)
                    .show();

        }
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (!(new AuthToken(getApplicationContext())).isEmpty()) {
            hideButtons();
            mAuthorizationManager.reAuthorize();
        } else {
            showButtons();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mThis = null;
        mIsAuthStart = false;
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mIsAuthStart = true;
        checkIntentForReauth();
        mThis = this;

        if (Data.isSSID() || (mProfileRequest != null && mProfileRequest.canceled)) {
            mIsAuthorized = true;
            hideButtons();
            getProfile(false);
        }

    }

    public static boolean isStarted() {
        return mIsAuthStart;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mAuthorizationManager.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_CANCELED) {
            hideButtons();
        } else {
            showButtons();
        }
    }

    @Override
    protected void onDestroy() {
        Debug.log(this, "-onDestroy");
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        if (!App.isOnline()) {
            Toast.makeText(this, getString(R.string.general_internet_off), Toast.LENGTH_SHORT)
                    .show();
        } else {
            if (view.getId() == R.id.btnAuthVK) {
                mAuthorizationManager.vkontakteAuth();
            } else if (view.getId() == R.id.btnAuthFB) {
                mAuthorizationManager.facebookAuth();
            }
        }
    }


    public void reAuthAfterInternetConnected(int type) {
        if (!mIsAuthorized) {
            if (!(new AuthToken(getApplicationContext()).isEmpty())) {
                mAuthorizationManager.reAuthorize();
                hideButtons();
                mRetryView.setVisibility(View.GONE);
                mProgressBar.setVisibility(View.VISIBLE);
            }
        }
        if (type == ConnectionChangeReceiver.CONNECTION_OFFLINE) mIsAuthorized = false;
    }

    private void showButtons() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //Эта проверка нужна, для безопасной работы в потоке
                if (mFBButton != null && mVKButton != null && mProgressBar != null) {
                    mFBButton.setVisibility(View.VISIBLE);
                    mVKButton.setVisibility(View.VISIBLE);
                    mProgressBar.setVisibility(View.GONE);
                }
            }
        });
    }

    private void hideButtons() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mFBButton.setVisibility(View.INVISIBLE);
                mVKButton.setVisibility(View.INVISIBLE);
                mProgressBar.setVisibility(View.VISIBLE);
            }
        });
    }

    private void openActivity(Intent intent) {
        ActivityManager mngr = (ActivityManager) getSystemService(ACTIVITY_SERVICE);

        List<ActivityManager.RunningTaskInfo> taskList = mngr.getRunningTasks(10);
        if (!mFromAuthorizationReceiver || (taskList.get(0).numActivities == 1 &&
                taskList.get(0).topActivity.getClassName().equals(this.getClass().getName()))) {
            startActivity(intent);
        }
        ConnectionManager.getInstance().notifyDelayedRequests();
        finish();
    }


    private void auth(AuthToken token) {
        AuthRequest authRequest = new AuthRequest(getApplicationContext());
        String socialNet = token.getSocialNet();
        EasyTracker.getTracker().trackEvent("Profile", "Auth", "FromActivity" + socialNet, 1L);
        registerRequest(authRequest);
        authRequest.platform = socialNet;
        authRequest.sid = token.getUserId();
        authRequest.token = token.getTokenKey();
        authRequest.callback(new ApiHandler() {
            @Override
            public void success(ApiResponse response) {
                Debug.log(this, "Auth");
                Auth auth = Auth.parse(response);
                Data.saveSSID(getApplicationContext(), auth.ssid);
                GCMUtils.init(AuthActivity.this);
                mIsAuthorized = true;

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        getProfile(true);
                    }
                });
            }

            @Override
            public void fail(final int codeError, ApiResponse response) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        authorizationFailed(codeError);
                        mIsAuthorized = false;
                    }
                });
            }

            @Override
            public void cancel() {
                showButtons();
            }
        }).exec();
    }

    private void getProfile(final boolean isFirstTime) {
        Debug.log("geting profile");
        mProfileRequest = new ProfileRequest(getApplicationContext());
        registerRequest(mProfileRequest);
        mProfileRequest.part = ProfileRequest.P_ALL;
        mProfileRequest.callback(new ApiHandler() {
            @Override
            public void success(final ApiResponse response) {
                CacheProfile.setProfile(Profile.parse(response), response);
                mProfileRequest = null;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        OptionsRequest request = new OptionsRequest(getApplicationContext());
                        ApiHandler handler = new ApiHandler() {

                            @Override
                            public void success(ApiResponse response) {
                                Options.parse(response);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (!isProfileNormal() && isFirstTime) {
                                            Intent intent = new Intent(AuthActivity.this, EditProfileActivity.class);
                                            intent.putExtra(EditProfileActivity.FROM_AUTH_ACTIVITY, true);
                                            openActivity(intent);
                                        } else {
                                            Intent intent = new Intent(AuthActivity.this, NavigationActivity.class);
                                            openActivity(intent);
                                        }
                                    }
                                });
                            }

                            @Override
                            public void fail(final int codeError, ApiResponse response) {
                                final ApiResponse finalResponse = response;
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (finalResponse.code == ApiResponse.BAN)
                                            showButtons();
                                        else {
                                            authorizationFailed(codeError);
                                            Toast.makeText(AuthActivity.this, getString(R.string.general_data_error),
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                        };
                        request.callback(handler);
                        request.exec();
                    }
                });
            }

            @Override
            public void fail(final int codeError, ApiResponse response) {
                final ApiResponse finalResponse = response;
                mProfileRequest = null;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (finalResponse.code == ApiResponse.BAN)
                            showButtons();
                        else {
                            authorizationFailed(codeError);
                            Toast.makeText(AuthActivity.this, getString(R.string.general_data_error),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }).exec();
    }

    private boolean isProfileNormal() {
        Profile profile = CacheProfile.getProfile();
        return (profile.age != 0 && profile.city_id != 0 && profile.photo != null);
    }

    private void checkIntentForReauth() {
        Bundle data = getIntent().getExtras();
        if (data != null) {
            if (data.get(ReAuthReceiver.REAUTH_FROM_RECEIVER) != null) {
                mFromAuthorizationReceiver = data.getBoolean(
                        ReAuthReceiver.REAUTH_FROM_RECEIVER, false);
            }
        }
    }

    private void authorizationFailed(int codeError) {
        hideButtons();
        switch (codeError) {
            case ApiResponse.NETWORK_CONNECT_ERROR:
                mRetryView.setErrorMsg(getString(R.string.general_reconnect_social));
                break;
            default:
                mRetryView.setErrorMsg(getString(R.string.general_data_error));
                break;
        }
        mRetryView.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.GONE);
    }
}
