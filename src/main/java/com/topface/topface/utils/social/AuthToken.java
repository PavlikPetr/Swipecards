package com.topface.topface.utils.social;

import android.content.Context;
import android.content.SharedPreferences;

import com.topface.framework.utils.Debug;
import com.topface.topface.App;
import com.topface.topface.Static;

public class AuthToken {
    // Data
    private TokenInfo mTokenInfo;
    private SharedPreferences mPreferences;
    // Constants
    public static final int AUTH_COMPLETE = 1001;
    public static final int AUTH_ERROR = 0;
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
