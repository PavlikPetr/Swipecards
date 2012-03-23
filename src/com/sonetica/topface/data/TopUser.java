package com.sonetica.topface.data;

import java.util.LinkedList;
import org.json.JSONArray;
import org.json.JSONObject;
import com.sonetica.topface.net.ApiResponse;
import com.sonetica.topface.utils.Debug;

/*
 * Класс профиля окна топов
 */
public class TopUser extends AbstractData {
  // Data
  public int uid;      // идентификатор красивого пользователя 
  public int liked;    // процент абсолютного значения красоты
  public String photo; // URL аватарки красивого пользователя
  //---------------------------------------------------------------------------
  public static LinkedList<TopUser> parse(ApiResponse response) {
    LinkedList<TopUser> userList = new LinkedList<TopUser>();
    
    try {
      JSONArray arr = response.mJSONResult.getJSONArray("top");
      if(arr.length()>0) 
        for(int i=0;i<arr.length();i++) {
          JSONObject item = arr.getJSONObject(i);
          TopUser topUser = new TopUser();
          topUser.liked   = item.optInt("liked");
          topUser.photo   = item.optString("photo");
          topUser.uid     = item.optInt("uid");
          userList.add(topUser);
        }
    } catch(Exception e) {
      Debug.log("TopUser.class","Wrong response parsing: " + e);
    }
    
    return userList;
  }
  //---------------------------------------------------------------------------
  @Override
  public String getBigLink() {
    return photo;
  }
  //---------------------------------------------------------------------------
  @Override
  public String getSmallLink() {
    return photo;
  }
  //---------------------------------------------------------------------------
}
