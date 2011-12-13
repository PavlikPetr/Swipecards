package com.sonetica.topface.social.fb;

import android.graphics.Bitmap;
import com.sonetica.topface.social.AuthToken;
import com.sonetica.topface.social.SnApi;

/*
 *  Класс для запросов к Facebook
 */
public class FbApi extends SnApi {
  // Data
  private static final String mApiUrl = "https://graph.facebook.com/";
  //---------------------------------------------------------------------------
  public FbApi(AuthToken.Token token) {
    super(token);
  }
  //---------------------------------------------------------------------------
  @Override
  public void getProfile() {
    
  }
  //---------------------------------------------------------------------------
  @Override
  public Bitmap getAvatar() {
    return null;
  }
  //---------------------------------------------------------------------------
  @Override
  protected String getApiUrl() {
    return mApiUrl;
  }
  //---------------------------------------------------------------------------
}//VkApi
