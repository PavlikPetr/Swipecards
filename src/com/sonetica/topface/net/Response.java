package com.sonetica.topface.net;

import java.util.ArrayList;
import java.util.LinkedList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.sonetica.topface.data.Album;
import com.sonetica.topface.data.City;
import com.sonetica.topface.data.Inbox;
import com.sonetica.topface.data.Like;
import com.sonetica.topface.data.Rate;
import com.sonetica.topface.data.TopUser;
import com.sonetica.topface.utils.Debug;

public class Response {
  // Data
  public int code;
  JSONObject mJsonResult;
  // Constants
  public static final int FATAL_ERROR = 99;
  //---------------------------------------------------------------------------
  public Response(String response) {
    if(response==null) {
      Debug.log(this,"json resonse is null");
      code = FATAL_ERROR;
      return;
    }
    JSONObject obj = null;
    try {
      obj = new JSONObject(response);
      if(!obj.isNull("error")) {
        mJsonResult = obj.getJSONObject("error");
        code = mJsonResult.getInt("code");
      } else if(!obj.isNull("result"))
        mJsonResult = obj.getJSONObject("result");
      else
        code = FATAL_ERROR;
    } catch (JSONException e) {
      code = FATAL_ERROR;
      Debug.log(this,"json resonse is wrong:" + response);
    }
  }
  //---------------------------------------------------------------------------
  public String getSSID() {
    if(code>0)
      return null;
    try {
      return mJsonResult.getString("ssid");
    } catch(JSONException e) {
      return null; 
    }
  }
  //---------------------------------------------------------------------------
  public ArrayList<TopUser> getUsers() {
    if(code>0)
      return null;
    ArrayList<TopUser> userList = null;
    try {
      JSONArray arr = mJsonResult.getJSONArray("top");
      if(arr.length()>0) {
        userList = new ArrayList<TopUser>();
        for(int i=0;i<arr.length();i++) {
          JSONObject item = arr.getJSONObject(i);
          TopUser topUser = new TopUser();
          topUser.liked = item.getString("liked");
          topUser.photo = item.getString("photo");
          topUser.uid   = item.getString("uid");
          userList.add(topUser);
        }
      }
    } catch(JSONException e) {
      return null;
    }
    return userList;
  }
  //---------------------------------------------------------------------------
  public ArrayList<City> getCities() {
    if(code>0)
      return null;
    ArrayList<City> cities = null;
    try {
      JSONArray arr = mJsonResult.getJSONArray("cities");
      if(arr.length()>0) {
        cities = new ArrayList<City>();
        for(int i=0;i<arr.length();i++) {
          JSONObject item = arr.getJSONObject(i);
          City city = new City();
          city.id   = Integer.parseInt(item.getString("id"));
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
    if(code>0)
      return null;
    return mJsonResult.toString();
  }
  //---------------------------------------------------------------------------
  public ArrayList<Album> getAlbum() {
    if(code>0)
      return null;
    ArrayList<Album> linkList = null;
    try {
      JSONArray arr = mJsonResult.getJSONArray("album");
      if(arr.length()>0) {
        linkList = new ArrayList<Album>();
        for(int i=0;i<arr.length();i++) {
          JSONObject item = arr.getJSONObject(i);
          Album album = new Album();
          album.id    = item.getString("id");
          album.small = item.getString("small");
          album.big   = item.getString("big");
          linkList.add(album);
        }
      }
      return linkList;
    } catch(JSONException e) {
      Debug.log(null,"error:"+e);
      return null; 
    }
  }
  //---------------------------------------------------------------------------
  public LinkedList<Inbox> getMessages() {
    if(code>0)
      return null;
    LinkedList<Inbox> userList = null;
    try {
      JSONArray arr = mJsonResult.getJSONArray("feed");
      if(arr.length()>0) {
        userList = new LinkedList<Inbox>();
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
  public LinkedList<Rate> getRates() {
    if(code>0)
      return null;
    LinkedList<Rate> rates = null;
    try {
      JSONArray arr = mJsonResult.getJSONArray("feed");
      if(arr.length()>0) {
        rates = new LinkedList<Rate>();
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
  public LinkedList<Like> getLikes() {
    if(code>0)
      return null;
    LinkedList<Like> rates = null;
    try {
      JSONArray arr = mJsonResult.getJSONArray("feed");
      if(arr.length()>0) {
        rates = new LinkedList<Like>();
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
