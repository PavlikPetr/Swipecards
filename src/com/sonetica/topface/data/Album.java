package com.sonetica.topface.data;

import java.util.LinkedList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.sonetica.topface.net.Response;
import com.sonetica.topface.utils.Debug;

public class Album extends AbstractData {
  // Data
  public int id;     // идентификатор фотографии в альбоме пользвоателя
  public String  small;  // строка URL маленького изображения пользователя
  public String  big;    // строка URL большого изображения пользователя
  public boolean ero;    // флаг, является ли фотография эротической
  public int cost;       // стоимость просмотра фотографии
  public int likes;      // количество одобрительных отзывов
  public int dislikes;   // количество отрицательных отзывов
  //---------------------------------------------------------------------------
  public static LinkedList<Album> parse(Response response) {
    LinkedList<Album> albumsList = new LinkedList<Album>();
    try {
      JSONArray array = response.mJSONResult.getJSONArray("album");
      if(array.length()>0)
        for(int i=0;i<array.length();i++) {
          JSONObject item = array.getJSONObject(i);
          Album album = new Album();
          album.id    = item.getInt("id");
          album.small = item.getString("small");
          album.big   = item.getString("big");
          
          if(!item.isNull("ero")) {
            album.ero   = true;
            album.cost  = item.getInt("cost");
            album.likes = item.getInt("likes");
            album.dislikes = item.getInt("dislikes");
          } else {
            album.ero = false;            
          }
          albumsList.add(album);
        }
    } catch(JSONException e) {
      Debug.log("Album.class","Wrong response parsing: " + e);
    }
    return albumsList; 
  }
  //---------------------------------------------------------------------------
  @Override
  public String getBigLink() {
    return big;
  }
  //---------------------------------------------------------------------------
  @Override
  public String getSmallLink() {
    return small;
  }
  //---------------------------------------------------------------------------
}
