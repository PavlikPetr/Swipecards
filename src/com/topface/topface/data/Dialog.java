package com.topface.topface.data;

import java.util.LinkedList;
import org.json.JSONArray;
import org.json.JSONObject;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.utils.Debug;

public class Dialog extends AbstractData {
    // Data
    public int type; // идентификатор типа сообщения диалога
    public String text; // текст сообщения, если type = MESSAGE
    public int gift; // идентификатор подарка. Поле определяется, если type = GIFT
    public String link; // ссылка на изображение подарка. Поле определяется, если type = GIFT
    public int id; // идентификатор события в ленте
    public int uid; // идентификатор отправителя
    public String first_name; // имя отправителя в текущей локали
    public int age; // возраст отправителя
    
    public int city_id; // идентификатор города
    public String city_name; // наименование города в локали указанной при авторизации
    public String city_full; // полное наименование города с указанием региона, если он определен. Отдается в локали пользователя, указанной при авторизации

    public boolean online; // флаг нахождения отправителя онлайн
    public boolean unread; // флаг причитанного диалога

    public String avatars_big; // фото большого размера
    public String avatars_small; // фото маленького размера

    public long created; // таймстамп отправления события
    public int target; // направление события в ленте. Возможные занчения: 0 - для исходящего события, 1 - для входящего события

    // Constants
    public static final int DEFAULT = 0;
    public static final int GIFT = 2;
    public static final int MESSAGE = 3;
    public static final int LIKE = 6;
    public static final int SYMPATHY = 7;
    public static final int WINK = 999;  // подмигивание

    public static final int TARGET_OUT = 0;
    public static final int TARGET_IN  = 1;
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

                dialog.type = item.optInt("type");
                dialog.text = item.optString("text");
                dialog.gift = item.optInt("gift");
                dialog.link = item.optString("link");
                dialog.id   = item.optInt("id");
                dialog.uid  = item.optInt("uid");
                dialog.first_name = item.optString("first_name");
                dialog.age  = item.optInt("age");

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
                
                dialog.created = item.optLong("created") * 1000; // время приходит в секундах *1000
                dialog.target  = item.optInt("target");

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
