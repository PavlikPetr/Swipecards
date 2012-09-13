package com.topface.topface.ui;

import com.topface.topface.ReAuthReceiver;
import com.topface.topface.R;
import com.topface.topface.Data;
import com.topface.topface.data.Auth;
import com.topface.topface.data.Profile;
import com.topface.topface.requests.ApiHandler;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.AuthRequest;
import com.topface.topface.requests.ProfileRequest;
import com.topface.topface.utils.AuthToken;
import com.topface.topface.utils.AuthorizationManager;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.http.ConnectionManager;
import com.topface.topface.utils.http.Http;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

public class AuthActivity extends BaseFragmentActivity implements View.OnClickListener {
    private Button mFBButton;
    private Button mVKButton;
    private ProgressBar mProgressBar;
    private AuthorizationManager mAuthorizationManager;

    private boolean mFromAuthorizationReceiver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Debug.log(this, "+onCreate");
        setContentView(R.layout.ac_auth);

        mAuthorizationManager = AuthorizationManager.getInstance(this);
        mAuthorizationManager.setOnAuthorizationHandler(new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case AuthorizationManager.AUTHORIZATION_FAILED:
                        showButtons();
                        break;
                    case AuthorizationManager.DIALOG_COMPLETED:
                        hideButtons();
                        break;
                    case AuthorizationManager.TOKEN_RECEIVED:
                        auth((AuthToken) msg.obj);
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

        if (!Http.isOnline(this))
            Toast.makeText(this, getString(R.string.general_internet_off), Toast.LENGTH_SHORT)
                    .show();

        if (Data.isSSID()) {
            hideButtons();
            getProfile();
        } else if (!(new AuthToken(getApplicationContext())).isEmpty()) {
            hideButtons();
            mAuthorizationManager.reAuthorize();
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
    protected void onResume() {
        super.onResume();
        checkIntentForReauth(getIntent());
    };

    @Override
    protected void onActivityResult(int requestCode,int resultCode,Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mAuthorizationManager.onActivityResult(requestCode, resultCode, data);
        hideButtons();
    }

    @Override
    protected void onDestroy() {
        Debug.log(this, "-onDestroy");
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btnAuthVK) {
            mAuthorizationManager.vkontakteAuth();
        } else if (view.getId() == R.id.btnAuthFB) {
            mAuthorizationManager.facebookAuth();
        }
    }

    private void showButtons() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mFBButton.setVisibility(View.VISIBLE);
                mVKButton.setVisibility(View.VISIBLE);
                mProgressBar.setVisibility(View.GONE);
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

    private void openNavigationActivity() {
        if (!mFromAuthorizationReceiver) {
            startActivity(new Intent(getApplicationContext(), NavigationActivity.class));
        } else {
            ConnectionManager.getInstance().notifyDelayedRequests();
        }
        finish();
    }

    private void auth(AuthToken token) {
        AuthRequest authRequest = new AuthRequest(getApplicationContext());
        registerRequest(authRequest);
        authRequest.platform = token.getSocialNet();
        authRequest.sid = token.getUserId();
        authRequest.token = token.getTokenKey();
        authRequest.callback(new ApiHandler() {
            @Override
            public void success(ApiResponse response) {
                Debug.log(this, "Auth");
                Auth auth = Auth.parse(response);
                Data.saveSSID(getApplicationContext(), auth.ssid);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        getProfile();
                    }
                });
            }

            @Override
            public void fail(int codeError, ApiResponse response) {
                showButtons();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(AuthActivity.this, getString(R.string.general_server_error),
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).exec();
    }

    private void getProfile() {
        //Profile.deleteProfile();
        if(Profile.isProfileExist()) {
            CacheProfile.setProfile(Profile.load());
            openNavigationActivity();
            return;
        }
        ProfileRequest profileRequest = new ProfileRequest(getApplicationContext());
        registerRequest(profileRequest);
        profileRequest.part = ProfileRequest.P_ALL;
        profileRequest.callback(new ApiHandler() {
            @Override
            public void success(final ApiResponse response) {
                CacheProfile.setProfile(Profile.parse(response));
                Http.avatarOwnerPreloading();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //Profile.save(profile);
                        Profile.save(response.mJSONResult.toString());
                        openNavigationActivity();
                    }
                });
            }

            @Override
            public void fail(int codeError, ApiResponse response) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(AuthActivity.this, getString(R.string.general_data_error),
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).exec();
    }

    private void checkIntentForReauth(Intent intent) {
        Bundle data = getIntent().getExtras();
        if (data != null) {
            if (data.get(ReAuthReceiver.REAUTH_FROM_RECEIVER) != null) {
                mFromAuthorizationReceiver = data.getBoolean(
                        ReAuthReceiver.REAUTH_FROM_RECEIVER, false);
            }
        }
    }
}
