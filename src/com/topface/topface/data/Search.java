package com.topface.topface.data;

import java.util.LinkedList;
import org.json.JSONArray;
import org.json.JSONObject;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.utils.Debug;

public class Search extends AbstractData {
    // Data
    public int uid; // идентификатор пользователя
    public int age; // возраст пользователя
    public int sex;
    public int city_id; // идентификатор города пользователя
    public boolean online; // флаг нахождения пользователя онлайн
    public String first_name; // имя пользователя
    public String status; // статус пользователя
    public String city_name; // наименование города пользователя
    public String city_full; // полное наименование города пользователя
    public String[] avatars_big; // большая аватарка пользователя
    public String[] avatars_small; // маленькая аватарка пользователя

    //public String geo_distance;  // дистация до пользователя (всегда NULL)
    //public String geo_coord;     // координаты пользователя
    //public String geo_coord_lat; // широта нахождения пользоавтеля
    //public String geo_coord_lng; // долгота нахождения пользователя
    //---------------------------------------------------------------------------
    public static LinkedList<Search> parse(ApiResponse response) {
        LinkedList<Search> userList = new LinkedList<Search>();

        try {
            JSONArray array = response.mJSONResult.getJSONArray("users");
            if (array.length() > 0)
                for (int i = 0; i < array.length(); i++) {
                    Search search = new Search();
                    JSONObject item = array.getJSONObject(i);
                    search.uid = item.optInt("uid");
                    search.age = item.optInt("age");
                    search.sex = item.optInt("sex");  
                    search.first_name = item.optString("first_name");
                    search.online = item.optBoolean("online");
                    search.status = item.optString("status");

                    // city
                    JSONObject city = item.getJSONObject("city");
                    search.city_id = city.optInt("id");
                    search.city_name = city.optString("name");
                    search.city_full = city.optString("full");

                    // avatars
                    JSONArray avatars = item.getJSONArray("avatars");
                    int size = avatars.length();
                    if (size > 0) {
                        search.avatars_big = new String[size];
                        search.avatars_small = new String[size];
                        for (int n = 0; n < avatars.length(); n++) {
                            JSONObject avatar = avatars.getJSONObject(n);
                            search.avatars_big[n] = avatar.optString("big");
                            search.avatars_small[n] = avatar.optString("small");
                        }
                    }
                    userList.add(search);
                }
        } catch(Exception e) {
            Debug.log("SearchUser.class", "Wrong response parsing: " + e);
        }

        return userList;
    }
    //---------------------------------------------------------------------------
    public int getUid() {
        return uid;
    };
    //---------------------------------------------------------------------------
    @Override
    public String getBigLink() {
        return avatars_big[0];
    }
    //---------------------------------------------------------------------------
    @Override
    public String getSmallLink() {
        return avatars_small[0];
    }
    //---------------------------------------------------------------------------
}
