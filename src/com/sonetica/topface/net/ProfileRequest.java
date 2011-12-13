package com.sonetica.topface.net;

import org.json.JSONException;
import org.json.JSONObject;

public class ProfileRequest extends Request {
  // Data
  public String service = "profile";
  // Methods
  @Override
  public String toString() {
    JSONObject root = new JSONObject();
    try {
      root.put("service",service);
      root.put("ssid",ssid);
      root.put("data",new JSONObject());
    } catch(JSONException e) {}
    return root.toString();
  }
}
