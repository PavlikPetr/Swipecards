package com.topface.topface.data;

import java.util.LinkedList;
import org.json.JSONArray;
import org.json.JSONObject;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.utils.Debug;

public class Dialog extends AbstractData {
    // Data
    public int type; // идентификатор типа сообщения диалога
    public int id; // идентификатор события в ленте
    public int uid; // идентификатор отправителя
    public int age; // возраст отправителя
    public String first_name; // имя отправителя в текущей локали
    public String text; // текст сообщения, если type = MESSAGE

    public int city_id; // идентификатор города
    public String city_name; // наименование города в локали указанной при авторизации
    public String city_full; // полное наименование города с указанием региона, если он определен. Отдается в локали пользователя, указанной при авторизации

    public boolean online; // флаг нахождения отправителя онлайн
    public boolean unread; // флаг причитанного диалога

    public String avatars_big; // фото большого размера
    public String avatars_small; // фото маленького размера

    public long created; // таймстамп отправления события
    // Constants
    public static final int DEFAULT = 0;
    public static final int MESSAGE = 3;
    public static final int LIKE = 6;
    public static final int SYMPATHY = 7;
    //--------------------------------------------------------------------------- 
    public static LinkedList<Dialog> parse(ApiResponse response) {
        LinkedList<Dialog> dialogList = new LinkedList<Dialog>();

        try {
            JSONArray arr = response.mJSONResult.getJSONArray("items");
            if (arr.length() > 0)
                dialogList = new LinkedList<Dialog>();
            for (int i = 0; i < arr.length(); i++) {
                JSONObject item = arr.getJSONObject(i);
                Dialog dialog = new Dialog();

                // TYPE
                dialog.type = item.optInt("type");

                dialog.id = item.optInt("id");
                dialog.uid = item.optInt("uid");
                dialog.age = item.optInt("age");
                dialog.first_name = item.optString("first_name");

                dialog.created = item.optLong("created"); // время приходит в секундах *1000

                dialog.text = item.optString("text");

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

                // avatars
                JSONObject avatars = item.getJSONObject("avatars");
                dialog.avatars_big = avatars.optString("big");
                dialog.avatars_small = avatars.optString("small");

                dialogList.add(dialog);
            }
        } catch(Exception e) {
            Debug.log("Dialog.class", "Wrong response parsing: " + e);
        }

        return dialogList;
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
