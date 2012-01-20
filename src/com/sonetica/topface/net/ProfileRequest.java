package com.sonetica.topface.net;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.sonetica.topface.utils.Debug;
import android.content.Context;

public class ProfileRequest extends ApiRequest {
  // Data
  private String  service = "profile";
  private String  fields;  //ассив интересующих полей профиля
  private boolean isNotification;
  //---------------------------------------------------------------------------
  public ProfileRequest(Context context,boolean bNotification) {
    super(context);
    isNotification = bNotification;
  }
  //---------------------------------------------------------------------------
  @Override
  public String toString() {
    JSONObject root = new JSONObject();
    try {
      root.put("service",service);
      root.put("ssid",ssid);
      if(isNotification)
        root.put("data",new JSONObject().put("fields",new JSONArray().put("unread_rates")
                                                                     .put("unread_likes")
                                                                     .put("unread_messages")));

    } catch(JSONException e) {
      Debug.log(this,"Wrong request compiling: " + e);
    }
    return root.toString();
  }
  //---------------------------------------------------------------------------
}
