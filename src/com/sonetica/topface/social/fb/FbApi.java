package com.sonetica.topface.social.fb;

import org.json.JSONObject;
import android.graphics.Bitmap;
import com.sonetica.topface.social.AuthToken;
import com.sonetica.topface.social.SnApi;
import com.sonetica.topface.social.SnRequest;

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
    JSONObject s = sendRequest(new SnRequest("me"));
    return null;
  }
  //---------------------------------------------------------------------------
  @Override
  protected String getApiUrl() {
    return mApiUrl;
  }
  //---------------------------------------------------------------------------
}//VkApi
