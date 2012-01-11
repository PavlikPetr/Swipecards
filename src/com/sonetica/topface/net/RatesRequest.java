package com.sonetica.topface.net;

import org.json.JSONException;
import org.json.JSONObject;
import android.content.Context;

public class RatesRequest extends ApiRequest {
  // Data
  public String service = "feedRates";
  public int offset;
  public int limit;
  //Methods
  public RatesRequest(Context context) {
    super(context);
  }
  @Override
  public String toString() {
    JSONObject root = new JSONObject();
    try {
      root.put("service",service);
      root.put("ssid",ssid);
      root.put("data",new JSONObject().put("offset",offset).put("limit",limit));
    } catch(JSONException e) {}
    return root.toString();
  }
}
