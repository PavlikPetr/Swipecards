package com.topface.topface.utils.social;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;

import com.facebook.topface.AsyncFacebookRunner;
import com.facebook.topface.Facebook;
import com.facebook.topface.FacebookError;
import com.topface.framework.utils.BackgroundThread;
import com.topface.framework.utils.Debug;
import com.topface.topface.App;
import com.topface.topface.Static;
import com.topface.topface.requests.ApiRequest;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKSdk;
import com.vk.sdk.VKSdkListener;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;

public class AuthToken {
    public static final int SUCCESS_GET_NAME = 0;
    public static final int FAILURE_GET_NAME = 1;
    // Data
    private TokenInfo mTokenInfo;
    private SharedPreferences mPreferences;
    public static final String TOKEN_NETWORK = "sn_type";
    public static final String TOKEN_USER_SOCIAL_ID = "user_id";
    public static final String TOKEN_TOKEN_KEY = "token_key";
    public static final String TOKEN_EXPIRES = "expires_in";
    public static final String TOKEN_LOGIN = "login";
    public static final String TOKEN_PASSWORD = "password";
    // SN Types
    public static final String SN_FACEBOOK = "fb";
    public static final String SN_VKONTAKTE = "vk";
    public static final String SN_TOPFACE = "st";
    public static final String SN_ODNOKLASSNIKI = "ok";

    private static AuthToken mInstance = new AuthToken();

    private AuthToken() {
        mPreferences = App.getContext().getSharedPreferences(Static.PREFERENCES_TAG_SHARED, Context.MODE_PRIVATE);
        mTokenInfo = new TokenInfo();
        loadToken();
    }

    public static AuthToken getInstance() {
        if (mInstance == null) {
            mInstance = new AuthToken();
        }
        return mInstance;
    }

    public static void getAccountName(Handler handler) {
        AuthToken authToken = getInstance();

        if (authToken.getSocialNet().equals(SN_FACEBOOK)) {
            getFbName(authToken.getUserSocialId(), handler);
        } else if (authToken.getSocialNet().equals(SN_VKONTAKTE)) {
            getVkName(authToken.getUserSocialId(), handler);
        } else if (authToken.getSocialNet().equals(SN_TOPFACE)) {
            handler.sendMessage(Message.obtain(null, SUCCESS_GET_NAME, authToken.getLogin()));
        }
        //Одноклассников здесь нет, потому что юзер запрашивается и сохраняется при авторизации
    }

    public static void getFbName(final String user_id, final Handler handler) {
        new AsyncFacebookRunner(new Facebook(App.getAppConfig().getAuthFbApi())).request("/" + user_id, new AsyncFacebookRunner.RequestListener() {

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

    public static void getVkName(final String user_id, final Handler handler) {
        VKSdk.initialize(new VKSdkListener() {
            @Override
            public void onCaptchaError(VKError vkError) {
            }

            @Override
            public void onTokenExpired(VKAccessToken vkAccessToken) {
            }

            @Override
            public void onAccessDenied(VKError vkError) {
            }
        }, Static.AUTH_VK_ID);
        new BackgroundThread() {
            @Override
            public void execute() {
                VKRequest request = VKApi.users().get(VKParameters.from(VKApiConst.USER_ID, user_id));
                request.attempts = ApiRequest.MAX_RESEND_CNT;
                request.secure = true;
                request.executeWithListener(new VKRequest.VKRequestListener() {
                    @Override
                    public void onComplete(VKResponse response) {
                        try {
                            String result = "";
                            JSONArray responseArr = response.json.getJSONArray("response");
                            if (responseArr != null) {
                                if (responseArr.length() > 0) {
                                    JSONObject profile = responseArr.getJSONObject(0);
                                    result = profile.optString("first_name") + " " + profile.optString("last_name");
                                }
                                handler.sendMessage(Message.obtain(null, AuthToken.SUCCESS_GET_NAME, result));
                            } else {
                                handler.sendMessage(Message.obtain(null, AuthToken.FAILURE_GET_NAME, ""));
                            }
                        } catch (Exception e) {
                            Debug.error("AuthorizationManager can't get name in vk", e);
                            handler.sendMessage(Message.obtain(null, AuthToken.FAILURE_GET_NAME, ""));
                        }
                    }
                });
            }
        };
    }


    private boolean isToken() {
        boolean hasSocialToken = (mTokenInfo.mTokenKey != null && mTokenInfo.mTokenKey.length() > 0);
        boolean hasTopfaceToken = (mTokenInfo.mLogin != null && mTokenInfo.mLogin.length() > 0
                && mTokenInfo.mPassword != null && mTokenInfo.mPassword.length() > 0);
        return mTokenInfo.mSnType.equals(SN_TOPFACE) ? hasTopfaceToken : hasSocialToken;
    }


    public void loadToken() {
        mTokenInfo.mSnType = mPreferences.getString(TOKEN_NETWORK, Static.EMPTY);
        mTokenInfo.mUserSocialId = mPreferences.getString(TOKEN_USER_SOCIAL_ID, Static.EMPTY);
        mTokenInfo.mTokenKey = mPreferences.getString(TOKEN_TOKEN_KEY, Static.EMPTY);
        mTokenInfo.mExpiresIn = mPreferences.getString(TOKEN_EXPIRES, Static.EMPTY);
        mTokenInfo.mLogin = mPreferences.getString(TOKEN_LOGIN, Static.EMPTY);
        mTokenInfo.mPassword = mPreferences.getString(TOKEN_PASSWORD, Static.EMPTY);
    }


    public void saveToken(String userSocialId, String login, String password) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(TOKEN_NETWORK, mTokenInfo.mSnType = SN_TOPFACE);
        editor.putString(TOKEN_USER_SOCIAL_ID, mTokenInfo.mUserSocialId = userSocialId);
        editor.putString(TOKEN_TOKEN_KEY, mTokenInfo.mTokenKey = Static.EMPTY);
        editor.putString(TOKEN_EXPIRES, mTokenInfo.mExpiresIn = Static.EMPTY);
        editor.putString(TOKEN_LOGIN, mTokenInfo.mLogin = login);
        editor.putString(TOKEN_PASSWORD, mTokenInfo.mPassword = password);
        editor.apply();
    }

    public void saveToken(String snType, String userSocialId, String tokenKey, String expiresIn) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(TOKEN_NETWORK, mTokenInfo.mSnType = snType);
        editor.putString(TOKEN_USER_SOCIAL_ID, mTokenInfo.mUserSocialId = userSocialId);
        editor.putString(TOKEN_TOKEN_KEY, mTokenInfo.mTokenKey = tokenKey);
        editor.putString(TOKEN_EXPIRES, mTokenInfo.mExpiresIn = expiresIn);
        editor.putString(TOKEN_LOGIN, mTokenInfo.mLogin = Static.EMPTY);
        editor.putString(TOKEN_PASSWORD, mTokenInfo.mPassword = Static.EMPTY);
        editor.apply();
    }

    public void removeToken() {
        saveToken(Static.EMPTY, Static.EMPTY, Static.EMPTY, Static.EMPTY);
    }

    public boolean isEmpty() {
        return mTokenInfo.mSnType.isEmpty() || !isToken();
    }

    public TokenInfo getTokenInfo() {
        try {
            return mTokenInfo.clone();
        } catch (CloneNotSupportedException e) {
            Debug.error(e.toString());
            return new TokenInfo();
        }
    }

    public AuthToken setTokeInfo(TokenInfo tokenInfo) {
        mTokenInfo.mSnType = tokenInfo.mSnType;
        mTokenInfo.mUserSocialId = tokenInfo.mUserSocialId;
        mTokenInfo.mTokenKey = tokenInfo.mTokenKey;
        mTokenInfo.mExpiresIn = tokenInfo.mExpiresIn;
        mTokenInfo.mLogin = tokenInfo.mLogin;
        mTokenInfo.mPassword = tokenInfo.mPassword;
        saveTokenInfo(tokenInfo);
        return this;
    }

    private void saveTokenInfo(TokenInfo tokenInfo) {
        if (tokenInfo.getSocialNet().equals(SN_TOPFACE)) {
            saveToken(tokenInfo.getUserSocialId(), tokenInfo.getLogin(), tokenInfo.getPassword());
        } else {
            saveToken(tokenInfo.getSocialNet(), tokenInfo.getUserSocialId(), tokenInfo.getTokenKey(), tokenInfo.getExpiresIn());
        }
    }

    public String getSocialNet() {
        return mTokenInfo.getSocialNet();
    }

    public String getTokenKey() {
        return mTokenInfo.getTokenKey();
    }

    public String getUserSocialId() {
        return mTokenInfo.getUserSocialId();
    }

    public String getUserTokenUniqueId() {
        return mTokenInfo.getUserTokenUniqueId();
    }

    public String getLogin() {
        return mTokenInfo.getLogin();
    }

    public String getPassword() {
        return mTokenInfo.getPassword();
    }

    @Override
    public String toString() {
        return getClass().getName() +
                Static.AMPERSAND + getSocialNet() +
                Static.AMPERSAND + getUserSocialId() +
                Static.AMPERSAND + getTokenKey();
    }

    public static class TokenInfo implements Cloneable {
        private String mSnType;
        private String mUserSocialId;
        private String mTokenKey;
        private String mExpiresIn;

        private String mLogin;
        private String mPassword;

        private TokenInfo() {
            mSnType = Static.EMPTY;
            mUserSocialId = Static.EMPTY;
            mTokenKey = Static.EMPTY;
            mExpiresIn = Static.EMPTY;
            mLogin = Static.EMPTY;
            mPassword = Static.EMPTY;
        }

        public String getLogin() {
            return mLogin;
        }

        public String getPassword() {
            return mPassword;
        }

        public String getSocialNet() {
            return mSnType;
        }

        public String getUserSocialId() {
            return mUserSocialId;
        }

        public String getTokenKey() {
            if (getSocialNet().equals(AuthToken.SN_FACEBOOK)) {
                return mTokenKey;
            } else if (getSocialNet().equals(AuthToken.SN_VKONTAKTE)) {
                return mTokenKey;
            } else if (getSocialNet().equals(AuthToken.SN_TOPFACE)) {
                return mLogin;
            } else if (getSocialNet().equals(SN_ODNOKLASSNIKI)) {
                return mTokenKey;
            }
            return mTokenKey;
        }

        @Override
        protected TokenInfo clone() throws CloneNotSupportedException {
            super.clone();
            TokenInfo tokenInfoClone = new TokenInfo();
            tokenInfoClone.mSnType = mSnType;
            tokenInfoClone.mUserSocialId = mUserSocialId;
            tokenInfoClone.mTokenKey = mTokenKey;
            tokenInfoClone.mExpiresIn = mExpiresIn;
            tokenInfoClone.mLogin = mLogin;
            tokenInfoClone.mPassword = mPassword;
            return tokenInfoClone;
        }

        public String getExpiresIn() {
            return mExpiresIn;
        }

        public String getUserTokenUniqueId() {
            return mSnType.equals(SN_TOPFACE) ? mLogin : mUserSocialId;
        }
    }
}
