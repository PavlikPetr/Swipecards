package com.sonetica.topface.social;

import android.graphics.Bitmap;

/*
 *  Класс для работы с Vkontakte
 */
public class VkApi extends SnApi {
  // Data
  private static final String mApiUrl = "https://api.vkontakte.ru/method/";
  //---------------------------------------------------------------------------
  public VkApi(AuthToken.Token token) {
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
