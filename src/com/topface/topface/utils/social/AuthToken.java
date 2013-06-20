package com.topface.topface.utils.social;

import android.content.Context;
import android.content.SharedPreferences;
import com.topface.topface.App;
import com.topface.topface.Static;

public class AuthToken {
    // Data
    private String mSnType;
    private String mUserId;
    private String mTokenKey;
    private String mExpiresIn;

    private String mLogin;
    private String mPassword;
    private SharedPreferences mPreferences;
    // Constants
    public static final int AUTH_COMPLETE = 1001;
    public static final int AUTH_ERROR = 0;
    public static final String TOKEN_NETWORK = "sn_type";
    public static final String TOKEN_USER_ID = "user_id";
    public static final String TOKEN_TOKEN_KEY = "token_key";
    public static final String TOKEN_EXPIRES = "expires_in";
    public static final String TOKEN_LOGIN = "login";
    public static final String TOKEN_PASSWORD = "password";
    // SN Types
    public static final String SN_FACEBOOK = "fb";
    public static final String SN_VKONTAKTE = "vk";
    public static final String SN_TOPFACE = "st";

    private static AuthToken mInstance = new AuthToken();

    private AuthToken() {
        mPreferences = App.getContext().getSharedPreferences(Static.PREFERENCES_TAG_SHARED, Context.MODE_PRIVATE);
        loadToken();
    }

    public static AuthToken getInstance() {
        if (mInstance == null) {
            mInstance = new AuthToken();
        }
        return mInstance;
    }


    public boolean isToken() {
        boolean hasSocialToken  = (mTokenKey != null && mTokenKey.length() > 0);
        boolean hasTopfaceToken = (mLogin != null && mLogin.length() > 0
                && mPassword != null && mPassword.length() > 0);
        return mSnType.equals(SN_TOPFACE) ? hasTopfaceToken : hasSocialToken;
    }


    public void loadToken() {
        mSnType = mPreferences.getString(TOKEN_NETWORK, Static.EMPTY);
        mUserId = mPreferences.getString(TOKEN_USER_ID, Static.EMPTY);
        mTokenKey = mPreferences.getString(TOKEN_TOKEN_KEY, Static.EMPTY);
        mExpiresIn = mPreferences.getString(TOKEN_EXPIRES, Static.EMPTY);
        mLogin  = mPreferences.getString(TOKEN_LOGIN,Static.EMPTY);
        mPassword  = mPreferences.getString(TOKEN_PASSWORD,Static.EMPTY);
    }


    public void saveToken(String user_Id, String login,String password) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(TOKEN_NETWORK, mSnType = SN_TOPFACE);
        editor.putString(TOKEN_USER_ID, mUserId = user_Id);
        editor.putString(TOKEN_TOKEN_KEY, mTokenKey = Static.EMPTY);
        editor.putString(TOKEN_EXPIRES, mExpiresIn = Static.EMPTY);
        editor.putString(TOKEN_LOGIN, mLogin = login);
        editor.putString(TOKEN_PASSWORD, mPassword = password);
        editor.commit();
    }

    public void saveToken(String sn_type, String user_Id, String token_key, String expires_in) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(TOKEN_NETWORK, mSnType = sn_type);
        editor.putString(TOKEN_USER_ID, mUserId = user_Id);
        editor.putString(TOKEN_TOKEN_KEY, mTokenKey = token_key);
        editor.putString(TOKEN_EXPIRES, mExpiresIn = expires_in);
        editor.putString(TOKEN_LOGIN, mLogin = Static.EMPTY);
        editor.putString(TOKEN_PASSWORD, mPassword = Static.EMPTY);
        editor.commit();
    }

    public void removeToken() {
        saveToken(Static.EMPTY, Static.EMPTY, Static.EMPTY, Static.EMPTY);
    }

    public String getSocialNet() {
        return mSnType;
    }

    public String getUserId() {
        return mUserId;
    }

    public String getTokenKey() {
        if (getSocialNet().equals(AuthToken.SN_FACEBOOK)){
            return mTokenKey;
        } else if (getSocialNet().equals(AuthToken.SN_VKONTAKTE)) {
            return mTokenKey;
        } else if (getSocialNet().equals(AuthToken.SN_TOPFACE)) {
            return mLogin;
        }
        return mTokenKey;
    }

    public boolean isEmpty() {
        return mSnType.equals(Static.EMPTY) || !isToken();
    }

    public String getLogin() {
        return mLogin;
    }

    public String getPassword() {
        return mPassword;
    }
}
