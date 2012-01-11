package com.sonetica.topface.net;

import org.json.JSONException;
import org.json.JSONObject;
import android.content.Context;

public class CitiesRequest extends ApiRequest {
  // Data
  public String service = "cities";
  public String type = "";
  // Methods
  public CitiesRequest(Context context) {
    super(context);
  }
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

