package com.sonetica.topface.requests;

import org.json.JSONException;
import org.json.JSONObject;
import com.sonetica.topface.utils.Debug;
import android.content.Context;

public class PhotoOpenRequest extends ApiRequest {
  // Data
  private String service = "photoOpen";
  public int uid;    // идентификатор пользователя хозяина фотографии
  public int photo;  // идентификатор эротической фотографии
  //---------------------------------------------------------------------------
  public PhotoOpenRequest(Context context) {
    super(context);
  }
  //---------------------------------------------------------------------------
  @Override
  public String toString() {
    JSONObject root = new JSONObject();
    try {
      root.put("service",service);
      root.put("ssid",ssid);
      root.put("data",new JSONObject().put("uid",uid)
                                      .put("photo",photo));
    } catch(JSONException e) {
      Debug.log(this,"Wrong request compiling: " + e);
    }
    
    return root.toString();
  }
  //---------------------------------------------------------------------------
}
