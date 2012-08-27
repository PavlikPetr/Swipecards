package com.topface.topface.data;

import java.util.LinkedList;
import org.json.JSONArray;
import org.json.JSONObject;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.utils.Debug;

public class FeedInbox extends AbstractDataWithPhotos {
    // Data
    public static int unread_count; // количество оставшихся непрочитанных
    public static boolean more; // имеются ли в ленте ещё симпатии для пользователя
    public int type; // тип сообщения
    public int id; // идентификатор сообщения 
    public int uid; // идентификатор отправителя
    public long created; // время отправления оценки
    public int target; // тип элемента ленты симпатий. Для данного запроса всегда 1 - входящее
    public boolean unread; // флаг прочитанного сообщения
    public String first_name; // имя пользователя    
    public int age; // возраст пользователя
    public boolean online; // флаг нахождения пользователя в онлайне
    public String text; // текст сообщения
    
    public int city_id; // идентификатор города отправителя сообщения
    public String city_name; // название города пользователя
    public String city_full; // полное название города пользвоателя
    
//    public int gift; // идентификатор подарка
//    public int code; // код входящего уведомления
//    public String avatars_big; // большая аватарка пользователя
//    public String avatars_small; // маленькая аватарка пользователя

    // Constants
    public static final int DEFAULT = 0; // По-умолчанию. Нигде не используется. Если возникает, наверное, надо что-то сделать
    public static final int PHOTO = 1; // Рекламное уведомление
    public static final int GIFT = 2; // Подарок
    public static final int MESSAGE = 3; // Текстовое сообщение
    public static final int MESSAGE_WISH = 4; // Тайное желание
    public static final int MESSAGE_SEXUALITY = 5; // Оценка сексуальности
 
    public static LinkedList<FeedInbox> parse(ApiResponse response) {
        LinkedList<FeedInbox> userList = new LinkedList<FeedInbox>();

        try {
            FeedInbox.unread_count = response.mJSONResult.getInt("unread");
            FeedInbox.more = response.mJSONResult.optBoolean("more");
            
            JSONArray arr = response.mJSONResult.getJSONArray("feed");
            for (int i = 0; i < arr.length(); i++) {
                JSONObject item = arr.getJSONObject(i);
                
                FeedInbox msg = new FeedInbox();
                msg.type = item.optInt("type");
                msg.id = item.optInt("id");
                msg.uid = item.optInt("uid");
                msg.created = item.optLong("created") * 1000; // время приходит в секундах *1000
                msg.target = item.optInt("target");
                msg.unread = item.optBoolean("unread");
                msg.first_name = item.optString("first_name");
                msg.age = item.optInt("age");
                msg.online = item.optBoolean("online");
                
                msg.text = item.optString("text");

//                switch (msg.type) {
//                    case DEFAULT:
//                        msg.text = item.optString("text");
//                        break;
//                    case PHOTO:
//                        msg.code = item.optInt("code");
//                        break;
//                    case GIFT:
//                        msg.gift = item.optInt("gift");
//                        break;
//                    case MESSAGE:
//                        msg.text = item.optString("text");
//                        break;
//                    case MESSAGE_WISH:
//                        break;
//                    case MESSAGE_SEXUALITY:
//                        break;
//                    default:
//                        break;
//                }

                // city  
                JSONObject city = item.optJSONObject("city");
                if (city != null) {
                    msg.city_id = city.optInt("id");
                    msg.city_name = city.optString("name");
                    msg.city_full = city.optString("full");
                } else {
                    msg.city_id = 0;
                    msg.city_name = "";
                    msg.city_full = "";
                }

                initPhotos(item, msg);
                
                // avatars
//                JSONObject avatars = item.getJSONObject("avatars");
//                msg.avatars_big = avatars.optString("big");
//                msg.avatars_small = avatars.optString("small");

                userList.add(msg);
            }
        } catch(Exception e) {
            Debug.log("FeedInbox.class", "Wrong response parsing: " + e);
        }

        return userList;
    }
    
    public int getUid() {
        return uid;
    };
}
