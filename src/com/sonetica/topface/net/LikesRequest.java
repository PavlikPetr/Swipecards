package com.sonetica.topface.net;

import org.json.JSONException;
import org.json.JSONObject;

public class LikesRequest extends Request {
  // Data
  public String service = "feedLike";
  public int offset;
  public int limit;
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
