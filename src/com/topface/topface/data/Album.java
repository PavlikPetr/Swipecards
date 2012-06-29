package com.topface.topface.data;

import java.util.LinkedList;
import org.json.JSONArray;
import org.json.JSONObject;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.utils.Debug;

public class Album extends AbstractData {
    // Data
    public int id; // идентификатор фотографии в альбоме пользвоателя
    public String small; // строка URL маленького изображения пользователя
    public String big; // строка URL большого изображения пользователя
    public boolean ero; // флаг, является ли фотография эротической
    public boolean buy; // является ли фотографию купленной текущим пользователем
    public int cost; // стоимость просмотра фотографии
    public int likes; // количество одобрительных отзывов
    public int dislikes; // количество отрицательных отзывов
    //---------------------------------------------------------------------------
    public static LinkedList<Album> parse(ApiResponse response) {
        LinkedList<Album> albumsList = new LinkedList<Album>();

        try {
            JSONArray array = response.mJSONResult.getJSONArray("album");
            if (array.length() > 0)
                for (int i = 0; i < array.length(); i++) {
                    JSONObject item = array.getJSONObject(i);
                    Album album = new Album();
                    album.id = item.optInt("id");
                    album.small = item.optString("small");
                    album.big = item.optString("big");

                    if (!item.isNull("ero")) {
                        album.ero = true;
                        album.buy = item.optBoolean("buy");
                        album.cost = item.optInt("cost");
                        album.likes = item.optInt("likes");
                        album.dislikes = item.optInt("dislikes");
                    } else
                        album.ero = false;
                    albumsList.add(album);
                }
        } catch(Exception e) {
            Debug.log("Album.class", "Wrong response parsing: " + e);
        }

        return albumsList;
    }
    //---------------------------------------------------------------------------
    public int getUid() {
        return id;
    };
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
