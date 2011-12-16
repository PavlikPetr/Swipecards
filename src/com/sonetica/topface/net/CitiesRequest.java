package com.sonetica.topface.net;

import org.json.JSONException;
import org.json.JSONObject;

public class CitiesRequest extends Request {
  // Data
  public String service = "cities";
  public String type = "";
  // Methods
  @Override
  public String toString() {
    JSONObject root = new JSONObject();
    try {
      root.put("service",service);
      root.put("ssid",ssid);
      root.put("data",new JSONObject().put("type",type));
    } catch(JSONException e) {}
    return root.toString();
  }
}
