package com.topface.topface.data;

import com.topface.topface.requests.ApiResponse;
import com.topface.topface.utils.Debug;

public class Auth extends AbstractData {
  // Data
  public int api_version;
  public String ssid;
  //---------------------------------------------------------------------------
  public static Auth parse(ApiResponse response) {
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
}
