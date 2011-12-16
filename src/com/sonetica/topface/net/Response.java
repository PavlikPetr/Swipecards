package com.sonetica.topface.net;

import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.sonetica.topface.data.City;
import com.sonetica.topface.data.Inbox;
import com.sonetica.topface.data.Like;
import com.sonetica.topface.data.Rate;
import com.sonetica.topface.data.TopUser;
import com.sonetica.topface.utils.Debug;

public class Response {
  // Data
  public int code = -1;
  JSONObject mJsonResult;
  //---------------------------------------------------------------------------
  public Response(String response) {
    JSONObject obj = null;
    try {
      obj = new JSONObject(response);
      if(!obj.isNull("error")) {
        mJsonResult = obj.getJSONObject("error");
        code = mJsonResult.getInt("code");
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
  public ArrayList<City> getCities() {
    if(code>=0)
      return null;
    ArrayList<City> cities = null;
    try {
      JSONArray arr = mJsonResult.getJSONArray("cities");
      if(arr.length()>0) {
        cities = new ArrayList<City>();
        for(int i=0;i<arr.length();i++) {
          JSONObject item = arr.getJSONObject(i);
          City city = new City();
          city.id   = item.getString("id");
          city.name = item.getString("name");
          cities.add(city);
        }
      }
    } catch(JSONException e) {
      return null;
    }
    return cities;
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
  public ArrayList<Inbox> getMessages() {
    if(code>=0)
      return null;
    ArrayList<Inbox> userList = null;
    try {
      JSONArray arr = mJsonResult.getJSONArray("feed");
      if(arr.length()>0) {
        userList = new ArrayList<Inbox>();
        for(int i=0;i<arr.length();i++) {
          JSONObject item = arr.getJSONObject(i);
          Inbox msg = new Inbox();
          msg.first_name = item.getString("first_name");
          msg.online = item.getString("online");
          msg.uid  = item.getString("uid");
          msg.age  = item.getString("age");
          msg.time = item.getString("time");
          msg.gift = item.isNull("gift")?null:item.getString("gift");
          msg.code = item.isNull("code")?null:item.getString("code");
          msg.text = item.isNull("text")?null:item.getString("text");
          userList.add(msg);
        }
      }
    } catch(JSONException e) {
      Debug.log(null,"");
    }
    return userList;
  }
  //---------------------------------------------------------------------------
  public ArrayList<Rate> getRates() {
    if(code>=0)
      return null;
    ArrayList<Rate> rates = null;
    try {
      JSONArray arr = mJsonResult.getJSONArray("feed");
      if(arr.length()>0) {
        rates = new ArrayList<Rate>();
        for(int i=0;i<arr.length();i++) {
          JSONObject item = arr.getJSONObject(i);
          Rate rate = new Rate();
          rate.first_name = item.getString("first_name");
          rate.online = item.getString("online");
          rate.uid    = item.getString("uid");
          rate.age    = item.getString("age");
          rate.rate   = item.getString("rate");
          JSONObject avatar  = item.getJSONObject("avatars");
          rate.avatars_small = avatar.getString("small");
          rate.avatars_big   = avatar.getString("big");
          rates.add(rate);
        }
      }
    } catch(JSONException e) {
      Debug.log(null,"");
    }
    return rates;
  }
  //---------------------------------------------------------------------------
  public ArrayList<Like> getLikes() {
    if(code>=0)
      return null;
    ArrayList<Like> rates = null;
    try {
      JSONArray arr = mJsonResult.getJSONArray("feed");
      if(arr.length()>0) {
        rates = new ArrayList<Like>();
        for(int i=0;i<arr.length();i++) {
          JSONObject item = arr.getJSONObject(i);
          Like like = new Like();
          like.first_name = item.getString("first_name");
          like.online = item.getString("online");
          like.uid    = item.getString("uid");
          JSONObject avatar  = item.getJSONObject("avatars");
          like.avatars_small = avatar.getString("small");
          like.avatars_big   = avatar.getString("big");
          rates.add(like);
        }
      }
    } catch(JSONException e) {
      Debug.log(null,"");
    }
    return rates;
  }
  //---------------------------------------------------------------------------
}
