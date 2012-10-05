package com.topface.topface.data;

import com.topface.topface.requests.ApiResponse;
import com.topface.topface.ui.adapters.FeedList;
import com.topface.topface.utils.Debug;
import org.json.JSONArray;
import org.json.JSONObject;

public class Dialog extends AbstractFeedItem {
    // Constants
    public static final int DEFAULT = 0; // По-умолчанию. Нигде не используется. Если возникает, наверное, надо что-то сделать
    public static final int PHOTO = 1; // Рекламное уведомление
    public static final int GIFT = 2; // Подарок
    public static final int MESSAGE = 3; // Текстовое сообщение
    public static final int MESSAGE_WISH = 4; // Тайное желание
    public static final int MESSAGE_SEXUALITY = 5; // Оценка сексуальности
    public static final int LIKE = 6; // Событие “понравилось”
    public static final int SYMPHATHY = 7; // Событие “симпатия”
    public static final int MESSAGE_WINK = 8; // подмигивание
    public static final int RATE = 9; // Оценка
    public static final int PROMOTION = 10; // Рекламное сообщение

    public static final int MAP = 11; // Текущее местоположение

    public static final int USER_MESSAGE = 0;
    public static final int FRIEND_MESSAGE = 1;

    public Dialog(ItemType retry) {
        super(retry);
    }

    public Dialog() {
        super();
    }

    public static FeedList<Dialog> parse(ApiResponse response) {
        FeedList<Dialog> dialogList = new FeedList<Dialog>();

        try {
            Dialog.unread_count = response.mJSONResult.getInt("unread");
            Dialog.more = response.mJSONResult.optBoolean("more");

            JSONArray arr = response.mJSONResult.getJSONArray("items");
            if (arr.length() > 0)
                dialogList = new FeedList<Dialog>();
            for (int i = 0; i < arr.length(); i++) {
                JSONObject item = arr.getJSONObject(i);
                Dialog dialog = new Dialog();

                dialog.type = item.optInt("type");
                dialog.id = item.optInt("id");
                dialog.uid = item.optInt("uid");
                dialog.created = item.optLong("created") * 1000;
                dialog.target = item.optInt("target");
                dialog.first_name = item.optString("first_name");
                dialog.sex = item.optInt("sex");
                dialog.age = item.optInt("age");
                dialog.online = item.optBoolean("online");

                // city  
                JSONObject city = item.optJSONObject("city");
                if (city != null) {
                    dialog.city_id = city.optInt("id");
                    dialog.city_name = city.optString("name");
                    dialog.city_full = city.optString("full");
                } else {
                    dialog.city_id = 0;
                    dialog.city_name = "";
                    dialog.city_full = "";
                }

                dialog.text = item.optString("text");

                initPhotos(item, dialog);
                dialogList.add(dialog);

            }
        } catch (Exception e) {
            Debug.log("Dialog.class", "Wrong response parsing: " + e);
        }

        return dialogList;
    }
}
