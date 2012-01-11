package com.sonetica.topface.net;

import org.json.JSONException;
import org.json.JSONObject;
import android.content.Context;

public class TopsRequest extends ApiRequest {
  // Data
  public String service = "top";
  public int sex;
  public int city;
  // Methods
  public TopsRequest(Context context) {
    super(context);
  }
  @Override
  public String toString() {
    JSONObject root = new JSONObject();
    try {
      root.put("service",service);
      root.put("ssid",ssid);
      root.put("data",new JSONObject().put("sex",sex).put("city",city));
    } catch(JSONException e) {}
    return root.toString();
  }
}
