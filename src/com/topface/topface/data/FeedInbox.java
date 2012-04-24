package com.topface.topface.data;

import java.util.LinkedList;
import org.json.JSONArray;
import org.json.JSONObject;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.utils.Debug;

public class FeedInbox extends AbstractData implements IAlbumData {
  // Data
  public static int unread_count; // количество оставшихся непрочитанных
  public int uid;              // идентификатор фотографии в альбоме пользвоателя
  public int age;              // возраст пользователя
  public int type;             // тип сообщения
  public int gift;             // идентификатор подарка
  public int code;             // код входящего уведомления
  public int city_id;          // идентификатор города отправителя сообщения
  public long created;         // время отправления оценки
  public boolean online;       // флаг нахождения пользователя в онлайне
  public boolean unread;       // флаг прочитанного сообщения
  public String city_name;     // название города пользователя
  public String city_full;     // полное название города пользвоателя
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
  public static LinkedList<FeedInbox> parse(ApiResponse response) {
    LinkedList<FeedInbox> userList = new LinkedList<FeedInbox>();
    
    try {
      JSONArray arr = response.mJSONResult.getJSONArray("feed");
      if(arr.length()>0)
        userList = new LinkedList<FeedInbox>();
        for(int i=0;i<arr.length();i++) {
          JSONObject item = arr.getJSONObject(i);
          FeedInbox.unread_count = response.mJSONResult.getInt("unread");
          FeedInbox msg = new FeedInbox();
            msg.first_name = item.optString("first_name");
            msg.online     = item.optBoolean("online");
            msg.unread     = item.optBoolean("unread");
            msg.created    = item.optLong("created"); // время приходит в секундах *1000
            msg.uid        = item.optInt("uid");
            msg.age        = item.optInt("age");
            msg.type       = item.optInt("type");
          
          switch(msg.type) {
            case DEFAULT:
              msg.text = item.optString("text");
              break;
            case PHOTO:
              msg.code = item.optInt("code");
              break;
            case GIFT:
              msg.gift = item.optInt("gift");
              break;
            case MESSAGE:
              msg.text = item.optString("text");
              break;
            case MESSAGE_WISH:
              break;
            case MESSAGE_SEXUALITY:
              break;
            default:
              break;
          }

          // city  
          JSONObject city = item.optJSONObject("city");
            if(city!=null) {
              msg.city_id    = city.optInt("id");            
              msg.city_name  = city.optString("name");
              msg.city_full  = city.optString("full");
            } else {
              msg.city_id    = 0;       
              msg.city_name  = "";
              msg.city_full  = "";
            }
            
          // avatars
          JSONObject avatars = item.getJSONObject("avatars");
            msg.avatars_big    = avatars.optString("big");
            msg.avatars_small  = avatars.optString("small");
            
          userList.add(msg);
        }
    } catch(Exception e) {
      Debug.log("FeedInbox.class","Wrong response parsing: " + e);
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
