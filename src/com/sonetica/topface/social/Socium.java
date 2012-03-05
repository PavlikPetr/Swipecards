package com.sonetica.topface.social;

import android.content.Context;
import android.net.Uri;

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
      mApi = new VkApi(context,token);
    else if(token.getSocialNet().equals(AuthToken.SN_FACEBOOK))
      mApi = new FbApi(context,token);
  }
  //---------------------------------------------------------------------------
  public void uploadPhoto(Uri uri){
    mApi.uploadPhoto(uri);
  }
  //---------------------------------------------------------------------------
  // class AuthException
  //---------------------------------------------------------------------------
  public static class AuthException extends Exception {
    private static final long serialVersionUID = 1L;
    public AuthException(String detailMessage) {
        super(detailMessage);
    }
  }
  //---------------------------------------------------------------------------
}
