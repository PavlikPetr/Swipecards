package com.topface.topface.utils.social;

import com.topface.topface.Static;
import android.content.Context;
import android.content.SharedPreferences;

public class AuthToken {
    // Data
    private String mSnType;
    private String mUserId;
    private String mTokenKey;
    private String mExpiresIn;
    private SharedPreferences mPreferences;
    // Constants
    public static final int AUTH_COMPLETE = 1001;
    public static final int AUTH_ERROR = 0;
    public static final String TOKEN_NETWORK = "sn_type";
    public static final String TOKEN_USER_ID = "user_id";
    public static final String TOKEN_TOKEN_KEY = "token_key";
    public static final String TOKEN_EXPIRES = "expires_in";
    // SN Types
    public static final String SN_FACEBOOK = "fb";
    public static final String SN_VKONTAKTE = "vk";
    //---------------------------------------------------------------------------
    public AuthToken(Context context) {
        mPreferences = context.getSharedPreferences(Static.PREFERENCES_TAG_SHARED, Context.MODE_PRIVATE);
        loadToken();
    }
    //---------------------------------------------------------------------------
    public boolean isToken() {
        return mTokenKey != null && mTokenKey.length() > 0;
    }
    //---------------------------------------------------------------------------
    public void loadToken() {
        mSnType = mPreferences.getString(TOKEN_NETWORK, Static.EMPTY);
        mUserId = mPreferences.getString(TOKEN_USER_ID, Static.EMPTY);
        mTokenKey = mPreferences.getString(TOKEN_TOKEN_KEY, Static.EMPTY);
        mExpiresIn = mPreferences.getString(TOKEN_EXPIRES, Static.EMPTY);
    }
    //---------------------------------------------------------------------------
    public void saveToken(String sn_type,String user_Id,String token_key,String expires_in) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(TOKEN_NETWORK, mSnType = sn_type);
        editor.putString(TOKEN_USER_ID, mUserId = user_Id);
        editor.putString(TOKEN_TOKEN_KEY, mTokenKey = token_key);
        editor.putString(TOKEN_EXPIRES, mExpiresIn = expires_in);
        editor.commit();
    }
    //---------------------------------------------------------------------------
    public void removeToken() {
        saveToken(Static.EMPTY, Static.EMPTY, Static.EMPTY, Static.EMPTY);
    }
    //---------------------------------------------------------------------------
    public String getSocialNet() {
        return mSnType;
    }
    //---------------------------------------------------------------------------
    public String getUserId() {
        return mUserId;
    }
    //---------------------------------------------------------------------------
    public String getTokenKey() {
        return mTokenKey;
    }    
    //---------------------------------------------------------------------------
    public String getExpires() {
        return mExpiresIn;
    }
    //---------------------------------------------------------------------------  
    public boolean isEmpty() {
    	if (mSnType.equals(Static.EMPTY)) {
    		return true;
    	} else {
    		return false;
    	}
    }
}
