package com.topface.topface.data;

import com.topface.topface.requests.ApiResponse;
import com.topface.topface.utils.Debug;
import org.json.JSONObject;

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
    public Photos photos;
    public Photo photo;
    public boolean rated;
    
    public static User parse(int userId, ApiResponse response) { //нужно знать userId
        User profile = new User();

        try {
            JSONObject item = response.jsonResult.getJSONObject("profiles");
            item = item.getJSONObject("" + userId);

            parse(profile, item);

            profile.platform = item.optString("platform");
            profile.last_visit = item.optInt("last_visit");
            profile.status = item.optString("status");
            profile.online = item.optBoolean("online");
            profile.ero = item.optBoolean("ero");
            profile.mutual = item.optBoolean("mutual");
            profile.score = item.optInt("score");
            profile.photo = new Photo(item.getJSONObject("photo"));
            profile.photos = Photos.parse(item.getJSONArray("photos"));

            initPhotos(item, profile);

        } catch (Exception e) {
            Debug.error("Wrong response parsing", e);
        }

        return profile;
    }

}
