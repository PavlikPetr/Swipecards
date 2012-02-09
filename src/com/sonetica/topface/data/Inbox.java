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
  public int age;              // возраст пользователя
  public int created;          // время отправления оценки
  public int type;             // тип сообщения
  public int city_id;          // идентификатор города отправителя сообщения
  public int gift;             // идентификатор подарка
  public int code;             // код входящего уведомления
  public boolean online;       // флаг нахождения пользователя в онлайне
  public boolean unread;       // флаг прочитанного сообщения
  public String first_name;    // имя пользователя
  public String avatars_big;   // большая аватарка пользователя
  public String avatars_small; // маленькая аватарка пользователя
  public String text;          // текст сообщения
  // Constants
  public static final int DEFAULT = 0;           // По-умолчанию. Нигде не используется. Если возникает, наверное, надо что-то сделать
  public static final int PHOTO   = 1;           // Рекламное уведомление
  public static final int GIFT    = 2;           // Подарок
  public static final int MESSAGE = 3;           // Текстовое сообщение
  public static final int MESSAGE_WISH = 4;      // Тайное желание
  public static final int MESSAGE_SEXUALITY = 5; // Оценка сексуальности
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
          msg.first_name = item.getString("first_name");
          msg.online     = item.getBoolean("online");
          msg.unread     = item.getBoolean("unread");
          msg.created    = item.getInt("created");
          msg.city_id    = item.getInt("city_id");
          msg.uid        = item.getInt("uid");
          msg.age        = item.getInt("age");
          msg.type       = item.getInt("type");
          
          switch(msg.type) {
            case DEFAULT:
              msg.text = item.getString("text");
              break;
            case PHOTO:
              msg.code = item.getInt("code");
              break;
            case GIFT:
              msg.gift = item.getInt("gift");
              break;
            case MESSAGE:
              msg.text = item.getString("text");
              break;
            case MESSAGE_WISH:
              break;
            case MESSAGE_SEXUALITY:
              break;
            default:
              break;
          }

          JSONObject avatars = item.getJSONObject("avatars");
          msg.avatars_big    = avatars.getString("big");
          msg.avatars_small  = avatars.getString("small");
          userList.add(msg);
        }
    } catch(JSONException e) {
      Debug.log("Inbox.class","Wrong response parsing: " + e);
    }
    return userList;
  }
  //---------------------------------------------------------------------------
  @Override
  public String getBigLink() {
    return avatars_big;
  }
  //---------------------------------------------------------------------------
  @Override
  public String getSmallLink() {
    return avatars_small;
  }
  //---------------------------------------------------------------------------
}
