package com.sonetica.topface.net;

import org.json.JSONException;
import org.json.JSONObject;
import com.sonetica.topface.utils.Debug;
import android.content.Context;

public class AuthRequest extends ApiRequest {
  // Data
  private String service = "auth";
  public  String sid;           // id пользователя в социальной сети
  public  String token;         // токен авторизации в соц сети
  public  String platform;      // код социальной сети
  public  String locale;        // локаль обращающегося клиента
  public  String clienttype;    // тип клиента
  public  String clientversion; // версия клиента
  public  String clientdevice;  // тип устройства клиента
  public  String clientid;      // уникальныц идентификатор клиентского устройства
  //---------------------------------------------------------------------------
  public AuthRequest(Context context) {
    super(context);
  }
  //---------------------------------------------------------------------------
  public String toString() {
    JSONObject root = new JSONObject();
    try {
      root.put("service",service);
      root.put("ssid",ssid);
      root.put("data",new JSONObject().put("sid",sid)
                                      .put("token",token)
                                      .put("platform",platform)           // vk, fb, mm, st
                                      .put("locale",locale)               // ru, en
                                      .put("clienttype",clienttype)       // iphone, ipod, ipad
                                      .put("clientversion",clientversion) // 1.2, 2.0
                                      .put("clientdevice",clientdevice)   // apple, htc, lg, samsung   
                                      .put("clientid",clientid));         // id
    } catch(JSONException e) {
      Debug.log(this,"Wrong request compiling: " + e);
    }
    
    return root.toString();
  }
  //---------------------------------------------------------------------------
}
