package com.topface.topface.utils.social;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.appevents.AppEventsConstants;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.topface.topface.App;
import com.topface.topface.utils.config.SessionConfig;

import java.util.Arrays;
import java.util.Collection;

/**
 * Class that starts Facebook authorization
 */
public class FbAuthorizer extends Authorizer {

    public static final String STAGE_AUTH_FACEBOOK_ID = "297350380464581";

    private static final String IS_FB_AUTHORIZED = "is_fb_authorized";
    private static final String FACEBOOK_REGISTRATION_METHOD = "Facebook";

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
                sendFaceBookEvent();
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

    private void sendFaceBookEvent() {
        AppEventsLogger logger = AppEventsLogger.newLogger(App.getContext());
        // отправляем ивент, что приложение запущено
        // да-да, на самом деле приложение было запущено раньше, но ивент шлем только при авторизации в FB
        logger.logEvent(AppEventsConstants.EVENT_NAME_ACTIVATED_APP);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(App.getContext());
        // проверяем флаг авторизации через FB, если авторизация производится в первый раз, то шлем ивент
        if (!preferences.getBoolean(IS_FB_AUTHORIZED, false)) {
            Bundle bundle = new Bundle();
            bundle.putString(AppEventsConstants.EVENT_PARAM_REGISTRATION_METHOD, FACEBOOK_REGISTRATION_METHOD);
            logger.logEvent(AppEventsConstants.EVENT_NAME_COMPLETED_REGISTRATION, bundle);
        }
        preferences.edit().putBoolean(IS_FB_AUTHORIZED, true).apply();
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
                ? STAGE_AUTH_FACEBOOK_ID
                : App.getAppSocialAppsIds().fbId;
    }

    @Override
    public void logout() {
        initFB();
        LoginManager.getInstance().logOut();
    }

    public static void initFB() {
        String appId = getFbId();
        if (!FacebookSdk.isInitialized() || !FacebookSdk.getApplicationId().equals(appId)) {
            FacebookSdk.setApplicationId(appId);
            FacebookSdk.sdkInitialize(App.getContext());
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    public static boolean isMainScreenLoginEnable() {
        return true;
    }
}
