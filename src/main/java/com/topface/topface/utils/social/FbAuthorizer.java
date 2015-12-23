package com.topface.topface.utils.social;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.topface.topface.App;
import com.topface.topface.Static;
import com.topface.topface.utils.config.SessionConfig;

import java.util.Arrays;
import java.util.Collection;

/**
 * Class that starts Facebook authorization
 */
public class FbAuthorizer extends Authorizer {

    private CallbackManager mCallbackManager;
    private ProfileTracker mProfileTracker;

    private Collection<String> PERMISSIONS = Arrays.asList("email", "public_profile", "user_friends", "user_photos", "user_birthday");

    public FbAuthorizer() {
        super();
        initFB();
        mCallbackManager = CallbackManager.Factory.create();
        mProfileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(
                    Profile oldProfile,
                    Profile currentProfile) {
                if (currentProfile != null) {
                    SessionConfig sessionConfig = App.getSessionConfig();
                    sessionConfig.setSocialAccountName(currentProfile.getName());
                    sessionConfig.saveConfig();
                }
            }
        };
        LoginManager.getInstance().registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                AccessToken accessToken = loginResult.getAccessToken();
                if (AuthToken.getInstance().isEmpty()) {
                    AuthToken.getInstance().saveToken(
                            AuthToken.SN_FACEBOOK,
                            accessToken.getUserId(),
                            accessToken.getToken(),
                            accessToken.getExpires().toString()
                    );
                }
            }

            @Override
            public void onCancel() {
                Intent intent = new Intent(AUTH_TOKEN_READY_ACTION);
                intent.putExtra(TOKEN_STATUS, TOKEN_NOT_READY);
                LocalBroadcastManager.getInstance(App.getContext()).sendBroadcast(intent);
            }

            @Override
            public void onError(FacebookException e) {
            }
        });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mProfileTracker.startTracking();
    }

    @Override
    public void authorize(Activity activity) {
        initFB();
        LoginManager.getInstance().logInWithReadPermissions(activity, PERMISSIONS);
    }

    public static String getFbId() {
        return App.getAppConfig().getStageChecked()
                ? Static.STAGE_AUTH_FACEBOOK_ID
                : App.getAppSocialAppsIds().fbId;
    }

    @Override
    public void logout() {
        initFB();
        LoginManager.getInstance().logOut();
    }

    private void initFB() {
        if (!FacebookSdk.isInitialized()) {
            FacebookSdk.setApplicationId(getFbId());
            FacebookSdk.sdkInitialize(App.getContext());
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }
}
