package com.topface.topface.data;

import android.text.TextUtils;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.utils.Debug;
import org.json.JSONObject;

/* Класс чужого профиля */
public class User extends Profile {
    public static final String EMPTY_JSON_ARRAY = "[]";
    // Data
    public String platform; // платформа пользователя
    public int last_visit;  // таймстамп последнего посещения приложения
    public String status;   // статус пользователя
    public boolean online;  // флаг наличия пользвоателя в онлайне
    public boolean mutual;  // флаг наличия симпатии к авторизованному пользователю
    public int score;       // средний балл оценок пользователя    
    public Photos photos;
    public Photo photo;
    public boolean rated;
    public int formMatches = 0;

    public static User parse(int userId, ApiResponse response) {
        User profile = new User();

        try {
            Object profilesTest = response.jsonResult.opt("profiles");
            if (!TextUtils.equals(profilesTest.toString(), EMPTY_JSON_ARRAY)) {
                JSONObject item = (JSONObject) profilesTest;
                item = item.getJSONObject("" + userId);

                parse(profile, item);

                profile.platform = item.optString("platform");
                profile.last_visit = item.optInt("last_visit");
                profile.status = item.optString("status");
                profile.online = item.optBoolean("online");
                profile.mutual = item.optBoolean("mailmutual");
                profile.score = item.optInt("score");
                profile.photo = new Photo(item.getJSONObject("photo"));
                profile.photos = Photos.parse(item.getJSONArray("photos"));

                initPhotos(item, profile);
            }

        } catch (Exception e) {
            Debug.error("Wrong response parsing", e);
        }

        return profile;
    }

}
