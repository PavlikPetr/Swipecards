package com.sonetica.topface.social;

import com.sonetica.topface.social.fb.FbApi;
import com.sonetica.topface.social.vk.VkApi;
import android.content.Context;
import android.graphics.Bitmap;

/*
 * Класс обертка над апи соц сетей
 */
public class Socium {
  // Data
  private SnApi mApi;
  //---------------------------------------------------------------------------
  public Socium(Context context) throws AuthException {
    AuthToken.Token token = new AuthToken(context).getToken();
    if(token == null)
      throw new AuthException("VkAuthToken is not valid or empty");
    
    if(token.getSocialNet().equals(AuthToken.SN_VKONTAKTE))
      mApi = new VkApi(token);
    else if(token.getSocialNet().equals(AuthToken.SN_FACEBOOK))
      mApi = new FbApi(token);
  }
  //---------------------------------------------------------------------------
  public Bitmap getAvatar(){
    return mApi.getAvatar();
  }
  //---------------------------------------------------------------------------
  // class AuthException
  //---------------------------------------------------------------------------
  public static class AuthException extends Exception {
    public AuthException(String detailMessage) {
        super(detailMessage);
    }
  }
  //---------------------------------------------------------------------------
}
