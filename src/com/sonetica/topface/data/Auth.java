package com.sonetica.topface.data;

import org.json.JSONException;
import org.json.JSONObject;
import com.sonetica.topface.utils.Debug;

public class Auth extends AbstractData {
  // Data
  public String ssid; //id (ssid) сессии нужен для подписи запросов к лицемеру
  //---------------------------------------------------------------------------
  public static Auth parse(JSONObject response) {
    Auth auth = new Auth();
    try {
      auth.ssid = response.getString("ssid");
    } catch(JSONException e) {
      Debug.log(null,"Wrong response parsing: " + e);
    }
    return auth;
  }
  //---------------------------------------------------------------------------
}
