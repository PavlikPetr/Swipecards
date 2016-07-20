package com.topface.topface.utils.social;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;

import com.topface.framework.utils.BackgroundThread;
import com.topface.framework.utils.Debug;
import com.topface.topface.App;
import com.topface.topface.requests.ApiRequest;
import com.topface.topface.utils.Utils;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;

import org.json.JSONArray;
import org.json.JSONObject;

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
        mPreferences = App.getContext().getSharedPreferences(App.PREFERENCES_TAG_SHARED, Context.MODE_PRIVATE);
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
            getFbName(handler);
        } else if (authToken.getSocialNet().equals(SN_VKONTAKTE)) {
            getVkName(authToken.getUserSocialId(), handler);
        } else if (authToken.getSocialNet().equals(SN_TOPFACE)) {
            handler.sendMessage(Message.obtain(null, SUCCESS_GET_NAME, authToken.getLogin()));
        }
        //Одноклассников здесь нет, потому что юзер запрашивается и сохраняется при авторизации
    }

    public static void getFbName(final Handler handler) {
//        Session session = Session.getActiveSession();
//        if (session != null) {
//            Request request = Request.newMeRequest(session, new Request.GraphUserCallback() {
//                @Override
//                public void onCompleted(GraphUser user, Response response) {
//                    if (user != null) {
//                        String name = user.getFirstName() + " " + user.getLastName();
//                        SessionConfig sessionConfig = App.getSessionConfig();
//                        sessionConfig.setSocialAccountName(name);
//                        sessionConfig.saveConfig();
//                        handler.sendMessage(Message.obtain(null, AuthToken.SUCCESS_GET_NAME, name));
//                    } else {
//                        handler.sendMessage(Message.obtain(null, AuthToken.FAILURE_GET_NAME, ""));
//                    }
//                }
//            });
//            request.executeAsync();
//        }
    }

    public static void getVkName(final String user_id, final Handler handler) {
        VkAuthorizer.initVkSdk();
        new BackgroundThread() {
            @Override
            public void execute() {
                VKRequest request = VKApi.users().get(VKParameters.from(VKApiConst.USER_ID, user_id));
                request.attempts = ApiRequest.MAX_RESEND_CNT;
                request.secure = true;
                try {
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
                } catch (NullPointerException e) {
                    Debug.error("Vkontakte bug https://github.com/VKCOM/vk-android-sdk/issues/89", e);
                    handler.sendMessage(Message.obtain(null, AuthToken.FAILURE_GET_NAME, ""));
                }
            }
        };
    }


    private boolean isToken() {
        boolean hasSocialToken = (mTokenInfo.mTokenKey != null && mTokenInfo.mTokenKey.length() > 0);
        boolean hasTopfaceToken = (mTokenInfo.mLogin != null && mTokenInfo.mLogin.length() > 0
                && mTokenInfo.mPassword != null && mTokenInfo.mPassword.length() > 0
                && mTokenInfo.mUserSocialId != null && mTokenInfo.mUserSocialId.length() > 0);
        return mTokenInfo.mSnType.equals(SN_TOPFACE) ? hasTopfaceToken : hasSocialToken;
    }


    public void loadToken() {
        mTokenInfo.mSnType = mPreferences.getString(TOKEN_NETWORK, Utils.EMPTY);
        mTokenInfo.mUserSocialId = mPreferences.getString(TOKEN_USER_SOCIAL_ID, Utils.EMPTY);
        mTokenInfo.mTokenKey = mPreferences.getString(TOKEN_TOKEN_KEY, Utils.EMPTY);
        mTokenInfo.mExpiresIn = mPreferences.getString(TOKEN_EXPIRES, Utils.EMPTY);
        mTokenInfo.mLogin = mPreferences.getString(TOKEN_LOGIN, Utils.EMPTY);
        mTokenInfo.mPassword = mPreferences.getString(TOKEN_PASSWORD, Utils.EMPTY);
    }


    public void saveToken(String userSocialId, String login, String password) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(TOKEN_NETWORK, mTokenInfo.mSnType = SN_TOPFACE);
        editor.putString(TOKEN_USER_SOCIAL_ID, mTokenInfo.mUserSocialId = userSocialId);
        editor.putString(TOKEN_TOKEN_KEY, mTokenInfo.mTokenKey = Utils.EMPTY);
        editor.putString(TOKEN_EXPIRES, mTokenInfo.mExpiresIn = Utils.EMPTY);
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
        editor.putString(TOKEN_LOGIN, mTokenInfo.mLogin = Utils.EMPTY);
        editor.putString(TOKEN_PASSWORD, mTokenInfo.mPassword = Utils.EMPTY);
        editor.apply();
    }

    /**
     * Сохраняем токен инфо в локальных переменных, чтоб все это умерло если мы свернем авторизацию
     * не дождавшисьответа от сервера. Дабы не получить залогиненое приложение с непрвильным токеном
     * fucking voodoo magic
     */
    public void temporarilySaveToken(String snType, String userSocialId, String tokenKey, String expiresIn){
        mTokenInfo.mSnType = snType;
        mTokenInfo.mUserSocialId = userSocialId;
        mTokenInfo.mTokenKey = tokenKey;
        mTokenInfo.mExpiresIn = expiresIn;
        mTokenInfo.mLogin = Utils.EMPTY;
        mTokenInfo.mPassword = Utils.EMPTY;
    }

    /**
     * Сохранить тукущи  токен в кэше в префиренсах
     */
    public void writeTokenInPreferences(){
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(TOKEN_NETWORK, mTokenInfo.mSnType);
        editor.putString(TOKEN_USER_SOCIAL_ID, mTokenInfo.mUserSocialId);
        editor.putString(TOKEN_TOKEN_KEY, mTokenInfo.mTokenKey);
        editor.putString(TOKEN_EXPIRES, mTokenInfo.mExpiresIn);
        editor.putString(TOKEN_LOGIN, mTokenInfo.mLogin);
        editor.putString(TOKEN_PASSWORD, mTokenInfo.mPassword);
        editor.apply();
    }

    public void removeToken() {
        saveToken(Utils.EMPTY, Utils.EMPTY, Utils.EMPTY, Utils.EMPTY);
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
                Utils.AMPERSAND + getSocialNet() +
                Utils.AMPERSAND + getUserSocialId() +
                Utils.AMPERSAND + getTokenKey();
    }

    public static class TokenInfo implements Cloneable, Parcelable {
        private String mSnType;
        private String mUserSocialId;
        private String mTokenKey;
        private String mExpiresIn;

        private String mLogin;
        private String mPassword;

        private TokenInfo() {
            mSnType = Utils.EMPTY;
            mUserSocialId = Utils.EMPTY;
            mTokenKey = Utils.EMPTY;
            mExpiresIn = Utils.EMPTY;
            mLogin = Utils.EMPTY;
            mPassword = Utils.EMPTY;
        }

        protected TokenInfo(Parcel in) {
            mSnType = in.readString();
            mUserSocialId = in.readString();
            mTokenKey = in.readString();
            mExpiresIn = in.readString();
            mLogin = in.readString();
            mPassword = in.readString();
        }

        public static final Creator<TokenInfo> CREATOR = new Creator<TokenInfo>() {
            @Override
            public TokenInfo createFromParcel(Parcel in) {
                return new TokenInfo(in);
            }

            @Override
            public TokenInfo[] newArray(int size) {
                return new TokenInfo[size];
            }
        };

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

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(mSnType);
            dest.writeString(mUserSocialId);
            dest.writeString(mTokenKey);
            dest.writeString(mExpiresIn);
            dest.writeString(mLogin);
            dest.writeString(mPassword);
        }
    }
}
