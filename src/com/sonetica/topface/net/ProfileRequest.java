package com.sonetica.topface.net;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ProfileRequest extends ApiRequest {
  // Data
  public String service = "profile";
  private boolean mIsNotification;
  // Methods
  public ProfileRequest() {
    this(false);
  }
  public ProfileRequest(boolean bNotification) {
    mIsNotification = bNotification;
  }
  @Override
  public String toString() {
    JSONObject root = new JSONObject();
    try {
      root.put("service",service);
      root.put("ssid",ssid);
      if(mIsNotification)
        root.put("data",new JSONObject().put("fields",new JSONArray().put("unread_rates").put("unread_likes").put("unread_messages")));
      //else
        //root.put("data","");
    } catch(JSONException e) {}
    return root.toString();
  }
}
