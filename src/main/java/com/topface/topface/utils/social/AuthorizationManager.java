package com.topface.topface.utils.social;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;

import com.facebook.topface.AsyncFacebookRunner;
import com.facebook.topface.AsyncFacebookRunner.RequestListener;
import com.facebook.topface.Facebook;
import com.facebook.topface.FacebookError;
import com.topface.framework.utils.BackgroundThread;
import com.topface.framework.utils.Debug;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.Ssid;
import com.topface.topface.Static;
import com.topface.topface.data.Auth;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.LogoutRequest;
import com.topface.topface.ui.NavigationActivity;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.cache.SearchCacheManager;
import com.topface.topface.utils.controllers.StartActionsController;
import com.topface.topface.utils.http.HttpUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

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

    private Activity mParentActivity;

    private Map<Platform, Authorizer> mAuthorizers = new HashMap<>();

    private static AuthorizationManager mInstance;

    public static AuthorizationManager getInstance(Activity parent) {
        if (mInstance == null) {
            synchronized (AuthorizationManager.class) {
                if (mInstance == null) {
                    mInstance = new AuthorizationManager(parent);
                }
            }
        }
        return mInstance;
    }

    // Constants
    private static final String VK_NAME_URL = "https://api.vk.com/method/getProfiles?uid=%s&access_token=%s";

    public void onCreate(Bundle savedInstanceState) {
        for (Authorizer authorizer : mAuthorizers.values()) {
            authorizer.onCreate(savedInstanceState);
        }
    }

    public void onResume() {
        for (Authorizer authorizer : mAuthorizers.values()) {
            authorizer.onResume();
        }
    }

    public void onDestroy() {
        for (Authorizer authorizer : mAuthorizers.values()) {
            authorizer.onDestroy();
        }
    }

    private AuthorizationManager(Activity parent) {
        mParentActivity = parent;
        mAuthorizers.put(Platform.VKONTAKTE, new VkAuthorizer(mParentActivity));
        mAuthorizers.put(Platform.FACEBOOK, new FbAuthorizer(mParentActivity));
        mAuthorizers.put(Platform.ODNOKLASSNIKI, new OkAuthorizer(mParentActivity));
        mAuthorizers.put(Platform.TOPFACE, new TfAuthorizer(mParentActivity));
    }

    public void refreshAccessToken() {
        mAuthorizers.get(Platform.FACEBOOK).refreshToken();
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

    public static void getAccountName(Handler handler) {
        AuthToken authToken = AuthToken.getInstance();

        if (authToken.getSocialNet().equals(AuthToken.SN_FACEBOOK)) {
            getFbName(authToken.getUserSocialId(), handler);
        } else if (authToken.getSocialNet().equals(AuthToken.SN_VKONTAKTE)) {
            getVkName(authToken.getTokenKey(), authToken.getUserSocialId(), handler);
        } else if (authToken.getSocialNet().equals(AuthToken.SN_TOPFACE)) {
            handler.sendMessage(Message.obtain(null, SUCCESS_GET_NAME, authToken.getLogin()));
        }
        //Одноклассников здесь нет, потому что юзер запрашивается и сохраняется при авторизации
    }


    public static final int SUCCESS_GET_NAME = 0;
    public static final int FAILURE_GET_NAME = 1;

    public static void getFbName(final String user_id, final Handler handler) {
        new AsyncFacebookRunner(new Facebook(App.getAppConfig().getAuthFbApi())).request("/" + user_id, new RequestListener() {

            @Override
            public void onComplete(String response, Object state) {
                try {
                    JSONObject jsonResult = new JSONObject(response);
                    String user_name = jsonResult.getString("name");
                    handler.sendMessage(Message.obtain(null, SUCCESS_GET_NAME, user_name));
                } catch (JSONException e) {
                    Debug.error("FB RequestListener::onComplete:error ", e);
                    handler.sendMessage(Message.obtain(null, FAILURE_GET_NAME, ""));
                }
            }

            @Override
            public void onMalformedURLException(MalformedURLException e, Object state) {
                Debug.error("FB RequestListener::onMalformedURLException");
                handler.sendMessage(Message.obtain(null, FAILURE_GET_NAME, ""));
            }

            @Override
            public void onIOException(IOException e, Object state) {
                Debug.error("FB RequestListener::onIOException", e);
                handler.sendMessage(Message.obtain(null, FAILURE_GET_NAME, ""));
            }

            @Override
            public void onFileNotFoundException(FileNotFoundException e, Object state) {
                Debug.error("FB RequestListener::onFileNotFoundException", e);
                handler.sendMessage(Message.obtain(null, FAILURE_GET_NAME, ""));
            }

            @Override
            public void onFacebookError(FacebookError e, Object state) {
                Debug.error("FB RequestListener::onFacebookError:" + state, e);
                handler.sendMessage(Message.obtain(null, FAILURE_GET_NAME, ""));
            }
        });
    }

    public static void getVkName(final String token, final String user_id, final Handler handler) {
        new BackgroundThread() {
            @Override
            public void execute() {
                String responseRaw = HttpUtils.httpGetRequest(String.format(Locale.ENGLISH, VK_NAME_URL, user_id, token));
                try {
                    String result = "";
                    JSONObject response = new JSONObject(responseRaw);
                    JSONArray responseArr = response.optJSONArray("response");
                    if (responseArr != null) {
                        if (responseArr.length() > 0) {
                            JSONObject profile = responseArr.getJSONObject(0);
                            result = profile.optString("first_name") + " " + profile.optString("last_name");
                        }
                        handler.sendMessage(Message.obtain(null, AuthorizationManager.SUCCESS_GET_NAME, result));
                    } else {
                        handler.sendMessage(Message.obtain(null, AuthorizationManager.FAILURE_GET_NAME, ""));
                    }
                } catch (Exception e) {
                    Debug.error("AuthorizationManager can't get name in vk", e);
                    handler.sendMessage(Message.obtain(null, AuthorizationManager.FAILURE_GET_NAME, ""));
                }
            }
        };
    }

    public void logout(Activity activity) {
        Ssid.remove();
        AuthToken authToken = AuthToken.getInstance();
        for (Authorizer authorizer : mAuthorizers.values()) {
            authorizer.logout();
        }
        authToken.removeToken();
        CacheProfile.clearProfileAndOptions();
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
        NavigationActivity.onLogout();
        if (!(activity instanceof NavigationActivity)) {
            activity.setResult(RESULT_LOGOUT);
            activity.finish();
        }
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
