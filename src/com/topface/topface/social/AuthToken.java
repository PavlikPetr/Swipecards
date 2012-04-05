package com.topface.topface.social;

import com.topface.topface.Global;
import android.content.Context;
import android.content.SharedPreferences;

/*
 * Класс для работы со структурой token ключа полученного из социальной сети 
 */
final public class AuthToken {
  //---------------------------------------------------------------------------
  // class Token ключ
  //---------------------------------------------------------------------------
  public static class Token {
    // Data
    private final String mSnType;
    private final String mUserId;
    private final String mTokenKey;
    private final String mExpiresIn;
    //Methods
    protected Token(String  snType, String  userId, String  tokenKey, String  expiresIn) {
      mSnType    = snType;
      mUserId    = userId;
      mTokenKey  = tokenKey;
      mExpiresIn = expiresIn;      
    }
    public String getSocialNet(){return mSnType;}
    public String getUserId(){return mUserId;}
    public String getTokenKey(){return mTokenKey;}
    public String getExpires(){return mExpiresIn;}
  }
  // Social Networks Types
  public static final String SN_FACEBOOK  = "fb";
  public static final String SN_VKONTAKTE = "vk";
  //---------------------------------------------------------------------------
  // Data
  private SharedPreferences mPreferences;
  // Constants
  public static final String DEFAULT_VALUE = "";
  // Auth Types
  public static final int AUTH_COMPLETE = 0;
  public static final int AUTH_ERROR    = 1;
  // Keys
  public static final String KEY_SOCIAL_NETWORK = "sn";
  public static final String KEY_USER_ID  = "user_id";
  public static final String KEY_TOKEN    = "token";
  public static final String KEY_EXPIRES  = "expires_in";
  //---------------------------------------------------------------------------
  public AuthToken(Context context) {
    mPreferences = context.getSharedPreferences(Global.TOKEN_PREFERENCES_TAG, Context.MODE_PRIVATE);
  }
  //---------------------------------------------------------------------------
  public Token setToken(String  snType, String  userId, String  tokenKey, String  expiresIn) {
    if(snType == null || userId == null || tokenKey == null || expiresIn == null)
      return null;
    
    SharedPreferences.Editor editor = mPreferences.edit();
    editor.putString(KEY_SOCIAL_NETWORK, snType);
    editor.putString(KEY_USER_ID, userId);
    editor.putString(KEY_TOKEN,   tokenKey);
    editor.putString(KEY_EXPIRES, expiresIn);
    
    if(!editor.commit())
      return null;

    return new Token(snType,userId,tokenKey,expiresIn); 
  }
  //---------------------------------------------------------------------------
  public Token getToken() {
    String tokenKey  = mPreferences.getString(KEY_TOKEN, DEFAULT_VALUE);
    if(tokenKey.equals(DEFAULT_VALUE))
      return null;

    String snType    = mPreferences.getString(KEY_SOCIAL_NETWORK, DEFAULT_VALUE);
    String userId    = mPreferences.getString(KEY_USER_ID, DEFAULT_VALUE);    
    String expiresIn = mPreferences.getString(KEY_EXPIRES, DEFAULT_VALUE);
    
    return new Token(snType,userId,tokenKey,expiresIn); 
  }
  //---------------------------------------------------------------------------
  public boolean isExist() {
    String tokenKey = mPreferences.getString(KEY_TOKEN, DEFAULT_VALUE);
    
    if(tokenKey.equals(DEFAULT_VALUE))
      return false;
    
    return true;
  }
  //---------------------------------------------------------------------------
  public void remove() {
    setToken(DEFAULT_VALUE, DEFAULT_VALUE, DEFAULT_VALUE, DEFAULT_VALUE);
  }
  //---------------------------------------------------------------------------
}//VkAuthToken
