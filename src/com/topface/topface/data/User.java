package com.topface.topface.data;

import com.topface.topface.requests.ApiResponse;
import com.topface.topface.utils.Debug;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.LinkedList;

/* Класс чужого профиля */
public class User extends Profile {
    // Data
    public String platform; // платформа пользователя
    public int last_visit;  // таймстамп последнего посещения приложения
    public String status;   // статус пользователя
    public boolean online;  // флаг наличия пользвоателя в онлайне
    public boolean ero;     // флаг наличия эротических фотографий
    public boolean mutual;  // флаг наличия симпатии к авторизованному пользователю
    public int score;       // средний балл оценок пользователя
    public LinkedList<Gift> gifts = new LinkedList<Gift>();

    public static User parse(int userId, ApiResponse response) { //нужно знать userId
        User profile = new User();

        try {
            JSONObject item = response.mJSONResult.getJSONObject("profiles");
            item = item.getJSONObject("" + userId);

            parse(profile, item);

            profile.platform = item.optString("platform");
            profile.last_visit = item.optInt("last_visit");
            profile.status = item.optString("status");
            profile.online = item.optBoolean("online");
            profile.ero = item.optBoolean("ero");
            profile.mutual = item.optBoolean("mutual");
            profile.score = item.optInt("score");

            //gifts
            JSONArray arrGifts = item.optJSONArray("gifts");
            for (int i = 0; i < arrGifts.length(); i++) {
                JSONObject itemGift = arrGifts.getJSONObject(i);
                Gift gift = new Gift();
                gift.id = itemGift.optInt("gift");
                gift.link = itemGift.optString("link");
                gift.type = Gift.PROFILE;
                gift.feedId = itemGift.optInt("id");
                profile.gifts.add(gift);
            }

            initPhotos(item, profile);

        } catch (Exception e) {
            Debug.log("User.class", "Wrong response parsing: " + e);
        }

        return profile;
    }

    public int getUid() {
        return uid;
    }

    ;

    @Override
    public String getLargeLink() {
        return null;
    }

    @Override
    public String getSmallLink() {
        return null;
    }
}
