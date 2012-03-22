package com.sonetica.topface.data;

import com.sonetica.topface.net.Response;
import com.sonetica.topface.utils.Debug;

public class Auth extends AbstractData {
  // Data
  public int api_version;
  public String ssid; //id (ssid) сессии нужен для подписи запросов к лицемеру
  //---------------------------------------------------------------------------
  public static Auth parse(Response response) {
    Auth auth = new Auth();
    
    try {
      auth.ssid = response.mJSONResult.getString("ssid");
      auth.api_version = response.mJSONResult.optInt("version");
    } catch(Exception e) {
      Debug.log("Auth.class","Wrong response parsing: " + e);
    }
    
    return auth;
  }
  //---------------------------------------------------------------------------
  @Override
  public String getBigLink() {
    return null;
  }
  //---------------------------------------------------------------------------
  @Override
  public String getSmallLink() {
    return null;
  }
  //---------------------------------------------------------------------------
}
