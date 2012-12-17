package com.topface.topface.data;

import com.topface.topface.requests.ApiResponse;
import com.topface.topface.utils.Debug;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.LinkedList;

/**
 * Класс профиля окна топов
 */
public class Top extends AbstractDataWithPhotos {
    /**
     * идентификатор красивого пользователя
     */
    public int uid;
    /**
     * процент абсолютного значения красоты
     */
    public int liked;
    /**
     * возраст
     */
    public int age;
    /**
     * имя
     */
    public String name;

    /**
     * онлайн статус
     */
    public boolean online;

    public static LinkedList<Top> parse(ApiResponse response) {
        LinkedList<Top> userList = new LinkedList<Top>();

        try {
            JSONArray arr = response.jsonResult.getJSONArray("users");
            if (arr.length() > 0)
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject item = arr.getJSONObject(i);
                    Top topUser = new Top();
                    topUser.liked = item.optInt("liked");
                    topUser.uid = item.optInt("id");
                    topUser.name = item.optString("first_name");
                    topUser.age = item.optInt("age");
                    topUser.online = item.optBoolean("online");
                    initPhotos(item, topUser);

                    userList.add(topUser);
                }
        } catch (Exception e) {
            Debug.error("TopUser.class: Wrong response parsing", e);
        }

        return userList;
    }

}
