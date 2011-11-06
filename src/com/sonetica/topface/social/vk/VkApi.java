package com.sonetica.topface.social.vk;

import android.graphics.Bitmap;
import com.sonetica.topface.social.AuthToken;
import com.sonetica.topface.social.SnApi;

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
