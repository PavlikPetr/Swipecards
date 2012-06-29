package com.topface.topface.data;

import java.util.LinkedList;
import org.json.JSONArray;
import org.json.JSONObject;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.utils.Debug;

public class FeedRate extends AbstractData {
    // Data
    public static int unread_count; // количество оставшихся непрочитанных
    public int uid; // идентификатор фотографии в альбоме пользвоателя
    public int age; // возраст пользователя
    public int rate; // оценка пользователя
    public int city_id; // идентификатор города отправителя оценки
    public long created; // время отправления оценки
    public boolean online; // флаг нахождения пользователя в онлайне
    public boolean unread; // флаг прочитанной оценки
    public String city_name; // название города пользователя
    public String city_full; // полное название города пользвоателя
    public String first_name; // имя пользователя
    public String avatars_big; // большая аватарка пользователя
    public String avatars_small; // маленькая аватарка пользователя
    //---------------------------------------------------------------------------
    public static LinkedList<FeedRate> parse(ApiResponse response) {
        LinkedList<FeedRate> ratesList = new LinkedList<FeedRate>();

        try {
            JSONArray arr = response.mJSONResult.getJSONArray("feed");
            if (arr.length() > 0)
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject item = arr.getJSONObject(i);
                    FeedRate rate = new FeedRate();
                    FeedRate.unread_count = response.mJSONResult.optInt("unread");
                    rate.first_name = item.optString("first_name");
                    rate.online = item.optBoolean("online");
                    rate.unread = item.optBoolean("unread");
                    rate.created = item.optLong("created"); // время приходит в секундах  *1000
                    rate.uid = item.optInt("uid");
                    rate.age = item.optInt("age");
                    rate.rate = item.optInt("rate");

                    // city  
                    JSONObject city = item.getJSONObject("city");
                    rate.city_id = city.optInt("id");
                    rate.city_name = city.optString("name");
                    rate.city_full = city.optString("full");

                    // avatars
                    JSONObject avatar = item.getJSONObject("avatars");
                    rate.avatars_small = avatar.optString("small");
                    rate.avatars_big = avatar.optString("big");
                    ratesList.add(rate);
                }
        } catch(Exception e) {
            Debug.log("FeedRate.class", "Wrong response parsing: " + e);
        }

        return ratesList;
    }
    //---------------------------------------------------------------------------
    public int getUid() {
        return uid;
    };
    //---------------------------------------------------------------------------
    @Override
    public String getBigLink() {
        return avatars_big;
    }
    //---------------------------------------------------------------------------
    @Override
    public String getSmallLink() {
        return avatars_small;
    }
    //---------------------------------------------------------------------------
}
