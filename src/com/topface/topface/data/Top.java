package com.topface.topface.data;

import com.topface.topface.requests.ApiResponse;
import com.topface.topface.utils.Debug;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.LinkedList;

/*
 * Класс профиля окна топов
 */
public class Top extends AbstractData {
    // Data
    public int uid;      // идентификатор красивого пользователя
    public int liked;    // процент абсолютного значения красоты
    public String photo; // URL аватарки красивого пользователя

    public static LinkedList<Top> parse(ApiResponse response) {
        LinkedList<Top> userList = new LinkedList<Top>();

        try {
            JSONArray arr = response.mJSONResult.getJSONArray("top");
            if (arr.length() > 0)
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject item = arr.getJSONObject(i);
                    Top topUser = new Top();
                    topUser.liked = item.optInt("liked");
                    topUser.photo = item.optString("photo");
                    topUser.uid = item.optInt("uid");
                    userList.add(topUser);
                }
        } catch (Exception e) {
            Debug.log("TopUser.class", "Wrong response parsing: " + e);
        }

        return userList;
    }

    public int getUid() {
        return uid;
    }

    ;

    @Override
    public String getBigLink() {
        return photo;
    }

    @Override
    public String getSmallLink() {
        return photo;
    }

}
