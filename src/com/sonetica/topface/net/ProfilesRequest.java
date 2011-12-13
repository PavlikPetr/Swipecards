package com.sonetica.topface.net;

import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ProfilesRequest extends Request {
  // Data
  public String service = "profiles";
  public ArrayList<Integer> uids = new ArrayList<Integer>();
  // Methods
  @Override
  public String toString() {
    JSONObject root = new JSONObject();
    try {
      root.put("service",service);
      root.put("ssid",ssid);
      root.put("data",new JSONObject().put("uids",new JSONArray(uids)));
    } catch(JSONException e) {}
    return root.toString();
  }
}
