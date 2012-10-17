package com.topface.topface.data;

import com.topface.topface.requests.ApiResponse;
import com.topface.topface.utils.Debug;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.LinkedList;

public class Search extends AbstractDataWithPhotos {
    // Data
    public int uid; // идентификатор пользователя
    public String first_name; // имя пользователя
    public int age; // возраст пользователя
    public String status; // статус пользователя
    public boolean online; // флаг нахождения пользователя онлайн
    public int sex;
    public int city_id; // идентификатор города пользователя
    public String city_name; // наименование города пользователя
    public String city_full; // полное наименование города пользователя
    public boolean mutual; // идентификатор пользователя, который послал симпатию, иначе 0

    // Flags
    public boolean skipped = false;
    public boolean rated = false;

    public static LinkedList<Search> parse(ApiResponse response) {
        LinkedList<Search> userList = new LinkedList<Search>();

        try {
            JSONArray array = response.jsonResult.getJSONArray("users");
            if (array.length() > 0)
                for (int i = 0; i < array.length(); i++) {
                    Search search = new Search();
                    JSONObject item = array.getJSONObject(i);
                    search.uid = item.optInt("uid");
                    search.first_name = item.optString("first_name");
                    search.age = item.optInt("age");
                    search.status = item.optString("status");
                    search.online = item.optBoolean("online");
                    search.sex = item.optInt("sex");
                    search.mutual = item.optBoolean("mutual");
                    search.photos = Photos.parse(item.getJSONArray("photos"));
                    JSONObject city = item.getJSONObject("city");
                    search.city_id = city.optInt("id");
                    search.city_name = city.optString("name");
                    search.city_full = city.optString("full");

                    initPhotos(item, search);

                    userList.add(search);
                }
        } catch (Exception e) {
            Debug.log("SearchUser.class", "Wrong response parsing: " + e);
        }

        return userList;
    }

}
