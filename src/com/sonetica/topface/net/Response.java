package com.sonetica.topface.net;

import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.sonetica.topface.data.TopUser;
import com.sonetica.topface.utils.Debug;

public class Response {
  // Data
  public int code = -1;
  JSONObject mJsonResult;
  //---------------------------------------------------------------------------
  public Response(String response) {
    Debug.log(null,"response:"+response);
    JSONObject obj = null;
    try {
      obj = new JSONObject(response);
      if(!obj.isNull("error")) {
        mJsonResult = obj.getJSONObject("error");
        code = obj.getInt("code");
      }
      mJsonResult = obj.getJSONObject("result");
    } catch (JSONException e) {
      // todo something
    }
  }
  //---------------------------------------------------------------------------
  public String getSsid() {
    if(code>=0)
      return null;
    try {
      return mJsonResult.getString("ssid");
    } catch(JSONException e) {
      return null; 
    }
  }
  //---------------------------------------------------------------------------
  public ArrayList<TopUser> getUsers() {
    if(code>=0)
      return null;
    ArrayList<TopUser> userList = null;
    try {
      JSONArray arr = mJsonResult.getJSONArray("top");
      if(arr.length()>0) {
        userList = new ArrayList<TopUser>();
        for(int i=0;i<arr.length();i++) {
          JSONObject item = arr.getJSONObject(i);
          userList.add(new TopUser(item.getString("uid"),item.getString("photo"),item.getString("liked")));
        }
      }
    } catch(JSONException e) {
      return null;
    }
    return userList;
  }
  //---------------------------------------------------------------------------
  public String getProfile() {
    if(code>=0)
      return null;
    try {
      return mJsonResult.getString("profile");
    } catch(JSONException e) {
      return null; 
    }
  }
  //---------------------------------------------------------------------------
  public ArrayList<String> getAlbum() {
    if(code>=0)
      return null;
    ArrayList<String> linkList = null;
    try {
      JSONArray arr = mJsonResult.getJSONArray("album");
      if(arr.length()>0) {
        linkList = new ArrayList<String>();
        for(int i=0;i<arr.length();i++) {
          JSONObject item = arr.getJSONObject(i);
          linkList.add(item.getString("big"));
        }
      }
      return linkList;
    } catch(JSONException e) {
      return null; 
    }
  }
//---------------------------------------------------------------------------
}
