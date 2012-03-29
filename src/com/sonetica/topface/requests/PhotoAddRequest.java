package com.sonetica.topface.requests;

import org.json.JSONException;
import org.json.JSONObject;
import com.sonetica.topface.utils.Debug;
import android.content.Context;

public class PhotoAddRequest extends ApiRequest {
  // Data
  private String service = "photoAdd";
  public String big;     // URL фотографии пользователя из социальной сети в большом разрешении
  public String medium;  // URL фотографии пользователя из социальной сети в среднем разрешении
  public String small;   // URL фотографии пользователя из социальной сети в малом разрешении
  public boolean ero;    // флаг, является ли фотография эротической
  public int cost;       // стоимость просмотра эротической фотографии
  //---------------------------------------------------------------------------
  public PhotoAddRequest(Context context) {
    super(context);
  }
  //---------------------------------------------------------------------------
  @Override
  public String toString() {
    JSONObject root = new JSONObject();
    try {
      root.put("service",service);
      root.put("ssid",ssid);
      root.put("data",new JSONObject().put("big",big)
                                      .put("medium",medium)
                                      .put("small",small)
                                      .put("ero",ero)
                                      .put("cost",cost));
    } catch(JSONException e) {
      Debug.log(this,"Wrong request compiling: " + e);
    }
    
    return root.toString();
  }
  //---------------------------------------------------------------------------
}
