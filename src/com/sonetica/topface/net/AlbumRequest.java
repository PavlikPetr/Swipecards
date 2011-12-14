package com.sonetica.topface.net;

import org.json.JSONException;
import org.json.JSONObject;

public class AlbumRequest extends Request {
  // Data
  public String service = "album";
  public int uid; 
  // Methods
  @Override
  public String toString() {
    JSONObject root = new JSONObject();
    try {
      root.put("service",service);
      root.put("ssid",ssid);
      root.put("data",new JSONObject().put("uid",uid));
    } catch(JSONException e) {}
    return root.toString();
  }
}

/*
{"result":
  {"album":
    [{"id":1,"small":"http:\/\/cs11364.vkontakte.ru\/u3770849\/a_a888a51f.jpg","big":"http:\/\/cs11364.vkontakte.ru\/u3770849\/a_a888a51f.jpg"},
     {"id":2,"small":"http:\/\/cs10488.vkontakte.ru\/u3770849\/e_486f0a11.jpg","big":"http:\/\/cs10488.vkontakte.ru\/u3770849\/a_dcd46022.jpg"},
     {"id":3,"small":"http:\/\/cs10488.vkontakte.ru\/u3770849\/e_f6f964ec.jpg","big":"http:\/\/cs10488.vkontakte.ru\/u3770849\/a_680a33dc.jpg"},
     {"id":4,"small":"http:\/\/cs4339.vkontakte.ru\/u3770849\/e_97517f8b.jpg","big":"http:\/\/cs4339.vkontakte.ru\/u3770849\/a_245b7ea1.jpg"},
     {"id":5,"small":"http:\/\/cs4339.vkontakte.ru\/u3770849\/e_97517f8b.jpg","big":"http:\/\/cs4339.vkontakte.ru\/u3770849\/a_245b7ea1.jpg"}]}}
*/