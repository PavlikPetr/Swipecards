package com.sonetica.topface.data;

import java.util.LinkedList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.sonetica.topface.net.Response;
import com.sonetica.topface.utils.Debug;

public class Inbox extends AbstractData {
  // Data
  public int uid;              // идентификатор фотографии в альбоме пользвоателя
  public String first_name;    // имя пользователя
  public int age;              // возраст пользователя
  public boolean online;       // флаг нахождения пользователя в онлайне
  public boolean unread;       // флаг прочитанного сообщения
  public int created;          // время отправления оценки
  public String avatars_big;   // большая аватарка пользователя
  public String avatars_small; // маленькая аватарка пользователя
  public int gift;             // идентификатор подарка
  public int code;             // код входящего уведомления
  public String text;          // текст сообщения
  //--------------------------------------------------------------------------- 
  public static LinkedList<Inbox> parse(Response response) {
    LinkedList<Inbox> userList = new LinkedList<Inbox>();
    try {
      JSONArray arr = response.mJSONResult.getJSONArray("feed");
      if(arr.length()>0)
        userList = new LinkedList<Inbox>();
        for(int i=0;i<arr.length();i++) {
          JSONObject item = arr.getJSONObject(i);
          Inbox msg = new Inbox();
          msg.first_name    = item.getString("first_name");
          msg.online        = item.getBoolean("online");
          msg.unread        = item.getBoolean("unread");
          msg.created       = item.getInt("created");
          msg.uid           = item.getInt("uid");
          msg.age           = item.getInt("age");
          msg.gift          = item.isNull("gift") ? 0 : item.getInt("gift");
          msg.code          = item.isNull("code") ? 0 : item.getInt("code");
          msg.text          = item.isNull("text") ? null : item.getString("text");
          JSONObject avatars = item.getJSONObject("avatars");
          msg.avatars_big   = avatars.getString("big");
          msg.avatars_small = avatars.getString("small");
          userList.add(msg);
        }
    } catch(JSONException e) {
      Debug.log("Inbox.class","Wrong response parsing: " + e);
    }
    return userList;
  }
  //---------------------------------------------------------------------------
}
