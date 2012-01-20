package com.sonetica.topface.data;

import java.util.LinkedList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.sonetica.topface.utils.Debug;

public class Album extends AbstractData {
  // Data
  public int     id;    // идентификатор фотографии в альбоме пользвоателя
  public String  small; // строка URL маленького изображения пользователя
  public String  big;   // строка URL большого изображения пользователя
  public boolean ero;   // флаг, является ли фотография эротической
  //---------------------------------------------------------------------------
  @Override
  public String getLink() {
    return big;
  }
  //---------------------------------------------------------------------------
  public static LinkedList<Album> parse(JSONObject response) {
    LinkedList<Album> albumsList = new LinkedList<Album>();
    try {
      JSONArray array = response.getJSONArray("album");
      if(array.length()>0)
        for(int i=0;i<array.length();i++) {
          JSONObject item = array.getJSONObject(i);
          Album album = new Album();
          album.id    = item.getInt("id");
          album.small = item.getString("small");
          album.big   = item.getString("big");
          album.ero   = item.getBoolean("ero");
          albumsList.add(album);
        }
    } catch(JSONException e) {
      Debug.log(null,"Wrong response parsing: " + e);
    }
    return albumsList; 
  }
  //---------------------------------------------------------------------------
}
