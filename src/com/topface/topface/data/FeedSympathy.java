package com.topface.topface.data;

import com.topface.topface.requests.ApiResponse;
import com.topface.topface.ui.adapters.IListLoader;
import com.topface.topface.utils.Debug;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.LinkedList;

/* Класс профиля окна топов */
public class FeedSympathy extends AbstractDataWithPhotos implements IListLoader {
    // Data
    public static int unread_count; // количество оставшихся непрочитанных
    public static boolean more; // имеются ли в ленте ещё симпатии для пользователя
    public int type; // тип сообщения
    public int id; // идентификатор симпатии в ленте
    public int uid; // идентификатор отправителя
    public long created; // таймстамп отправления симпатии
    public int target; // тип элемента ленты симпатий. Для данного запроса всегда 1 - входящее
    public boolean unread; // флаг прочитанной симпатии
    public String first_name; // имя отправителя в текущей локали
    public int age; // возраст отправителя
    public boolean online; // флаг нахождения отправителя онлайн

    public int city_id; // идентификатор города
    public String city_name; // наименование города в локали указанной при авторизации
    public String city_full; // полное наименование города с указанием региона, если он определен. Отдается в локали пользователя, указанной при авторизации

//    public String avatars_big; // фото большого размера
//    public String avatars_small; // фото маленького размера

    public boolean isListLoader = false;
    public boolean isListLoaderRetry = false;

    public FeedSympathy() {

    }

    public FeedSympathy(IListLoader.ItemType type) {
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

    public static LinkedList<FeedSympathy> parse(ApiResponse response) {
        LinkedList<FeedSympathy> symphatyList = new LinkedList<FeedSympathy>();

        try {
            FeedSympathy.unread_count = response.mJSONResult.optInt("unread");
            FeedSympathy.more = response.mJSONResult.optBoolean("more");

            JSONArray arr = response.mJSONResult.getJSONArray("feed");
            if (arr.length() > 0)
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject item = arr.getJSONObject(i);

                    FeedSympathy symphaty = new FeedSympathy();
                    symphaty.type = item.optInt("type");
                    symphaty.id = item.optInt("id");
                    symphaty.uid = item.optInt("uid");
                    symphaty.created = item.optLong("created") * 1000;
                    symphaty.target = item.optInt("target");
                    symphaty.unread = item.optBoolean("unread");
                    symphaty.first_name = item.optString("first_name");
                    symphaty.age = item.optInt("age");
                    symphaty.online = item.optBoolean("online");

                    // city  
                    JSONObject city = item.getJSONObject("city");
                    symphaty.city_id = city.optInt("id");
                    symphaty.city_name = city.optString("name");
                    symphaty.city_full = city.optString("full");

                    initPhotos(item, symphaty);

                    // avatars
//                    JSONObject avatar = item.getJSONObject("avatars");
//                    symphaty.avatars_big = avatar.optString("big");
//                    symphaty.avatars_small = avatar.optString("small");

                    symphatyList.add(symphaty);
                }
        } catch (Exception e) {
            Debug.log("FeedSymphaty.class", "Wrong response parsing: " + e);
        }

        return symphatyList;
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
