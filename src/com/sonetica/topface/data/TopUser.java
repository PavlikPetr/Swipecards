package com.sonetica.topface.data;

import java.util.LinkedList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.sonetica.topface.net.Response;
import com.sonetica.topface.utils.Debug;

/*
 * Структура для хранения профиля в топе
 */
public class TopUser extends AbstractData {
  // Data
  public int uid;      // идентификатор красивого пользователя
  public String photo; // URL аватарки красивого пользователя 
  public int liked;    // процент абсолютного значения красоты
  //---------------------------------------------------------------------------
  @Override
  public String getLink() {
    return photo;
  }
  //---------------------------------------------------------------------------
  public static LinkedList<TopUser> parse(Response response) {
    LinkedList<TopUser> userList = new LinkedList<TopUser>();
    try {
      JSONArray arr = response.mJSONResult.getJSONArray("top");
      if(arr.length()>0) 
        for(int i=0;i<arr.length();i++) {
          JSONObject item = arr.getJSONObject(i);
          TopUser topUser = new TopUser();
          topUser.liked   = item.getInt("liked");
          topUser.photo   = item.getString("photo");
          topUser.uid     = item.getInt("uid");
          userList.add(topUser);
        }
    } catch(JSONException e) {
      Debug.log("TopUser.class","Wrong response parsing: " + e);
    }
    return userList;
  }
  //---------------------------------------------------------------------------
}
