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
    public String[] avatars_big; // большая аватарка пользователя
    public String[] avatars_small; // маленькая аватарка пользователя

    //public String geo_distance;  // дистация до пользователя (всегда NULL)
    //public String geo_coord;     // координаты пользователя
    //public String geo_coord_lat; // широта нахождения пользоавтеля
    //public String geo_coord_lng; // долгота нахождения пользователя

    // Flags
    public boolean skipped = false;
    public boolean rated = false;

    public static LinkedList<Search> parse(ApiResponse response) {
        LinkedList<Search> userList = new LinkedList<Search>();

        try {
            JSONArray array = response.mJSONResult.getJSONArray("users");
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

                    // avatars
//                    JSONArray avatars = item.getJSONArray("avatars");
//                    int size = avatars.length();
//                    if (size > 0) {
//                        search.avatars_big = new String[size];
//                        search.avatars_small = new String[size];
//                        for (int n = 0; n < avatars.length(); n++) {
//                            JSONObject avatar = avatars.getJSONObject(n);
//                            search.avatars_big[n] = avatar.optString("big");
//                            search.avatars_small[n] = avatar.optString("small");
//                        }
//                    }

                    // city
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

    public int getUid() {
        return uid;
    }

    public String getSmallLink() {
        //TODO: Переписать на нормальные Photo объекты
        return avatars_small[0];
    }

}
