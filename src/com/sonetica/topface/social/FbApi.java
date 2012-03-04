package com.sonetica.topface.social;

import android.content.Context;

/*
 *  Класс для запросов к Facebook
 */
public class FbApi extends SnApi {
  // Data
  private static final String mApiUrl = "https://graph.facebook.com/";
  //---------------------------------------------------------------------------
  public FbApi(Context context,AuthToken.Token token) {
    super(context,token);
  }
  //---------------------------------------------------------------------------
  @Override
  public void getProfile() {
    
  }
  //---------------------------------------------------------------------------
  @Override
  public void uploadPhoto() {
    
    StringBuilder request = new StringBuilder("https://graph.facebook.com/me/photos?access_token=");
    request.append("&access_token=" + mToken.getTokenKey());
    
  }
  //---------------------------------------------------------------------------
  @Override
  protected String getApiUrl() {
    return mApiUrl;
  }
  //---------------------------------------------------------------------------
}//VkApi
