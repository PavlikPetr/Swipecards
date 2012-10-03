package com.topface.topface.data;

import com.topface.topface.requests.ApiResponse;
import com.topface.topface.utils.Debug;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.LinkedList;

public class FeedGifts extends AbstractDataWithPhotos {
    // Data    
    public static boolean more; // имеются ли в ленте ещё подарки для пользователя
    public int type; // тип сообщения
    public int id; // идентификатор сообщения 
    public int uid; // идентификатор отправителя
    public long created; // время отправления оценки
    public int target; // тип элемента ленты симпатий. Для данного запроса всегда 1 - входящее
    public boolean unread; // флаг прочитанного сообщения
    public String first_name; // имя пользователя    
    public int age; // возраст пользователя
    public boolean online; // флаг нахождения пользователя в онлайне

    public int city_id; // идентификатор города отправителя сообщения
    public String city_name; // название города пользователя
    public String city_full; // полное название города пользвоателя

    public Gift gift;

    public static LinkedList<FeedGifts> parse(ApiResponse response) {
        LinkedList<FeedGifts> feedsList = new LinkedList<FeedGifts>();

        try {
            FeedGifts.more = response.mJSONResult.optBoolean("more");

            JSONArray arr = response.mJSONResult.getJSONArray("feed");
            for (int i = 0; i < arr.length(); i++) {
                JSONObject item = arr.getJSONObject(i);

                FeedGifts feed = new FeedGifts();
                feed.type = item.optInt("type");
                feed.id = item.optInt("id");
                feed.uid = item.optInt("uid");
                feed.created = item.optLong("created") * 1000; // время приходит в секундах *1000
                feed.target = item.optInt("target");
                feed.unread = item.optBoolean("unread");
                feed.first_name = item.optString("first_name");
                feed.age = item.optInt("age");
                feed.online = item.optBoolean("online");

                // city  
                JSONObject city = item.optJSONObject("city");
                if (city != null) {
                    feed.city_id = city.optInt("id");
                    feed.city_name = city.optString("name");
                    feed.city_full = city.optString("full");
                } else {
                    feed.city_id = 0;
                    feed.city_name = "";
                    feed.city_full = "";
                }

                // gift
                feed.gift = new Gift();
                feed.gift.id = item.optInt("gift");
                feed.gift.link = item.optString("link");
                feed.gift.type = Gift.PROFILE;

                initPhotos(item, feed);
                feedsList.add(feed);
            }
        } catch (Exception e) {
            Debug.log("FeedGifts.class", "Wrong response parsing: " + e);
        }

        return feedsList;
    }

    public int getUid() {
        return uid;
    }

    ;
}

