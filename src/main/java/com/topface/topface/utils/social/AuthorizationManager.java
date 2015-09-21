package com.topface.topface.utils.social;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import com.topface.framework.utils.BackgroundThread;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.Ssid;
import com.topface.topface.Static;
import com.topface.topface.data.Auth;
import com.topface.topface.data.Options;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.LogoutRequest;
import com.topface.topface.state.TopfaceAppState;
import com.topface.topface.ui.NavigationActivity;
import com.topface.topface.ui.fragments.feed.TabbedDialogsFragment;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.ads.AdmobInterstitialUtils;
import com.topface.topface.utils.cache.SearchCacheManager;
import com.topface.topface.utils.controllers.StartActionsController;
import com.topface.topface.utils.notifications.UserNotificationManager;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

/**
 * AuthorizationManager has to be attached to some Activity (setted on getInstance(...))
 * onActivityResult(...) put to the parent Activity.onActivityResult(...)
 * Use handler setted with setOnAuthorizationHandler() to receive authorization events
 * message.what = AUTHORIZATION_FAILED - authorization failed for some reasons
 * message.what = TOKEN_RECEIVED | message.obj = authToken - token received from SN
 * message.what = DIALOG_COMPLETED - dialog for login closed
 *
 * @author kirussell
 */

public class AuthorizationManager {

    public static final int RESULT_LOGOUT = 666;
    @Inject
    TopfaceAppState mAppState;

    private Map<Platform, Authorizer> mAuthorizers = new HashMap<>();

    public void onCreate(Bundle savedInstanceState) {
        App.from(App.getContext()).inject(this);
        for (Authorizer authorizer : mAuthorizers.values()) {
            authorizer.onCreate(savedInstanceState);
        }
    }

    public void onResume() {
        for (Authorizer authorizer : mAuthorizers.values()) {
            authorizer.onResume();
        }
    }

    public void onPause() {
        for (Authorizer authorizer : mAuthorizers.values()) {
            authorizer.onPause();
        }
    }

    public void onDestroy() {
        for (Authorizer authorizer : mAuthorizers.values()) {
            authorizer.onDestroy();
        }
    }

    public AuthorizationManager(Activity parent) {
        mAuthorizers.put(Platform.VKONTAKTE, new VkAuthorizer(parent));
        mAuthorizers.put(Platform.FACEBOOK, new FbAuthorizer(parent));
        mAuthorizers.put(Platform.ODNOKLASSNIKI, new OkAuthorizer(parent));
        mAuthorizers.put(Platform.TOPFACE, new TfAuthorizer(parent));
    }

    public static void saveAuthInfo(IApiResponse response) {
        Auth auth = new Auth(response);
        saveAuthInfo(auth);
    }

    public static void saveAuthInfo(Auth auth) {
        Ssid.save(auth.ssid);
        AuthToken token = AuthToken.getInstance();
        if (token.getSocialNet().equals(AuthToken.SN_TOPFACE)) {
            token.saveToken(auth.userId, token.getLogin(), token.getPassword());
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        for (Authorizer authorizer : mAuthorizers.values()) {
            authorizer.onActivityResult(requestCode, resultCode, data);
        }
    }

    // vkontakte methods
    public void vkontakteAuth() {
        mAuthorizers.get(Platform.VKONTAKTE).authorize();
    }

    // Facebook methods
    public void facebookAuth() {
        mAuthorizers.get(Platform.FACEBOOK).authorize();
    }

    public void odnoklassnikiAuth() {
        mAuthorizers.get(Platform.ODNOKLASSNIKI).authorize();
    }

    public void topfaceAuth() {
        mAuthorizers.get(Platform.TOPFACE).authorize();
    }

    public void logout(Activity activity) {
        Ssid.remove();
        UserNotificationManager.getInstance().removeNotifications();
        TabbedDialogsFragment.setTabsDefaultPosition();
        AuthToken authToken = AuthToken.getInstance();
        for (Authorizer authorizer : mAuthorizers.values()) {
            authorizer.logout();
        }
        authToken.removeToken();
        mAppState.destroyObservable(Options.class);
        CacheProfile.clearProfileAndOptions(mAppState);
        App.getConfig().onLogout();
        StartActionsController.onLogout();
        SharedPreferences preferences = activity.getSharedPreferences(Static.PREFERENCES_TAG_SHARED, Context.MODE_PRIVATE);
        if (preferences != null) {
            preferences.edit().clear().apply();
        }
        LocalBroadcastManager.getInstance(activity).sendBroadcast(new Intent(Static.LOGOUT_INTENT));
        //Чистим список тех, кого нужно оценить
        new BackgroundThread() {
            @Override
            public void execute() {
                new SearchCacheManager().clearCache();
            }
        };
        if (!(activity instanceof NavigationActivity)) {
            activity.setResult(RESULT_LOGOUT);
            activity.finish();
        }
        AdmobInterstitialUtils.onLogout();
    }

    public static void showRetryLogoutDialog(Activity activity, final LogoutRequest logoutRequest) {
        AlertDialog.Builder retryBuilder = new AlertDialog.Builder(activity);
        retryBuilder.setMessage(R.string.general_logout_error)
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .setPositiveButton(R.string.auth_retry, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        logoutRequest.exec();
                    }
                });
        retryBuilder.create().show();
    }

}
