package com.sonetica.topface.data;

import org.json.JSONException;
import com.sonetica.topface.net.Response;
import com.sonetica.topface.utils.Debug;

public class Auth extends AbstractData {
  // Data
  public String ssid; //id (ssid) сессии нужен для подписи запросов к лицемеру
  //---------------------------------------------------------------------------
  public static Auth parse(Response response) {
    Auth auth = new Auth();
    try {
      auth.ssid = response.mJSONResult.getString("ssid");
    } catch(JSONException e) {
      Debug.log("Auth.class","Wrong response parsing: " + e);
    }
    return auth;
  }
  //---------------------------------------------------------------------------
}
