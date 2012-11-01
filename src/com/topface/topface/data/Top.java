package com.topface.topface.data;

import com.topface.topface.requests.ApiResponse;
import com.topface.topface.utils.Debug;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.LinkedList;

/**
 * Класс профиля окна топов
 */
public class Top {
    /**
     * идентификатор красивого пользователя
     */
    public int uid;
    /**
     * URL аватарки красивого пользователя
     */
    public String photo;
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
            JSONArray arr = response.jsonResult.getJSONArray("top");
            if (arr.length() > 0)
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject item = arr.getJSONObject(i);
                    Top topUser = new Top();
                    topUser.liked = item.optInt("liked");
                    topUser.photo = item.optString("photo");
                    topUser.uid = item.optInt("uid");
                    topUser.name = item.optString("name");
                    topUser.age = item.optInt("age");
                    topUser.online = item.optBoolean("online");

                    userList.add(topUser);
                }
        } catch (Exception e) {
            Debug.log("TopUser.class", "Wrong response parsing: " + e);
        }

        return userList;
    }

}
