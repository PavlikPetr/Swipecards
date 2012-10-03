package com.topface.topface.data;

import com.topface.topface.requests.ApiResponse;
import com.topface.topface.ui.adapters.IListLoader;
import com.topface.topface.utils.Debug;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.LinkedList;

public class FeedLike extends AbstractDataWithPhotos implements IListLoader {
    // Data
    public static int unread_count; // количество оставшихся непрочитанных
    public static boolean more; // имеются ли в ленте ещё симпатии для пользователя
    public int type; // тип сообщения
    public int id; // идентификатор сообщения 
    public int uid; // идентификатор отправителя
    public long created; // таймштамп отправления “понравилось”
    public int target; // тип элемента ленты симпатий. Для данного запроса всегда 1 - входящее
    public boolean unread; // флаг прочитанного лайка
    public String first_name; // имя пользователя
    public int age; // возраст пользователя
    public boolean online; // флаг нахождения пользователя в онлайне
    public int city_id; // идентификатор города отправителя оценки
    public String city_name; // название города пользователя
    public String city_full; // полное название города пользвоателя

//    public String avatars_big; // большая аватарка пользователя
//    public String avatars_small; // маленькая аватарка пользователя
//    public int rate; // значение “понравилось”

    public boolean isListLoader = false;
    public boolean isListLoaderRetry = false;

    public FeedLike() {

    }

    public FeedLike(IListLoader.ItemType type) {
        switch (type) {
            case LOADER:
                isListLoader = true;
                break;
            case RETRY:
                isListLoaderRetry = true;
                break;
            default:
                break;
        }
    }

    public static LinkedList<FeedLike> parse(ApiResponse response) {
        LinkedList<FeedLike> likesList = new LinkedList<FeedLike>();

        try {
            FeedLike.unread_count = response.mJSONResult.getInt("unread");
            FeedLike.more = response.mJSONResult.optBoolean("more");

            JSONArray arr = response.mJSONResult.getJSONArray("feed");
            if (arr.length() > 0)
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject item = arr.getJSONObject(i);

                    FeedLike like = new FeedLike();
                    like.type = item.optInt("type");
                    like.id = item.optInt("id");
                    like.uid = item.optInt("uid");
                    like.created = item.optLong("created") * 1000;
                    like.target = item.optInt("target");
                    like.unread = item.optBoolean("unread");
                    like.first_name = item.optString("first_name");
                    like.age = item.optInt("age");
                    like.online = item.optBoolean("online");

                    // city  
                    JSONObject city = item.getJSONObject("city");
                    like.city_id = city.optInt("id");
                    like.city_name = city.optString("name");
                    like.city_full = city.optString("full");

                    // avatars
//                    JSONObject avatar = item.optJSONObject("avatars");
//                    if (avatar != null) {
//	                    like.avatars_big = avatar.optString("big");
//	                    like.avatars_small = avatar.optString("small");
//                    }

//                    like.rate = item.optInt("rate");

                    initPhotos(item, like);

                    likesList.add(like);
                }
        } catch (Exception e) {
            Debug.log("FeedLike.class", "Wrong response parsing: " + e);
        }

        return likesList;
    }

    public int getUid() {
        return uid;
    }

    ;

    @Override
    public boolean isLoader() {
        return isListLoader;
    }

    @Override
    public boolean isLoaderRetry() {
        return isListLoaderRetry;
    }

    @Override
    public void switchToLoader() {
        isListLoader = false;
        isListLoaderRetry = true;
    }
}
