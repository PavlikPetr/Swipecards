package com.sonetica.topface.net;

import org.json.JSONException;
import org.json.JSONObject;

public class AuthRequest extends Request {
  // Data
  public String service = "auth";
  public String sid;
  public String token;
  public String platform;
  // Methods
  @Override
  public String toString() {
    JSONObject root = new JSONObject();
    try {
      root.put("service",service);
      root.put("ssid",ssid);
      root.put("data",new JSONObject().put("sid",sid).put("token",token).put("platform",platform));
    } catch(JSONException e) {}
    return root.toString();
  }
}
