package com.topface.topface.data;

import com.topface.topface.requests.ApiResponse;
import com.topface.topface.utils.Debug;

import org.json.JSONObject;

/* Класс чужого профиля */
public class User extends Profile {
    // Data
    public String platform; // платформа пользователя
    public int lastVisit;  // таймстамп последнего посещения приложения
    public String status;   // статус пользователя
    public boolean online;  // флаг наличия пользвоателя в онлайне
    public boolean mutual;  // флаг наличия симпатии к авторизованному пользователю
    public int score;       // средний балл оценок пользователя    
    public int formMatches = 0;
    public boolean banned;
    public boolean deleted;
    public boolean bookmarked;

    public static User parse(int userId, ApiResponse response) {
        User user = new User();

        try {
            JSONObject item = response.getJsonResult();
            if (item != null) {
                parse(user, item);
                user.platform = item.optString("platform");
                user.lastVisit = item.optInt("lastVisit");
                user.inBlackList = item.optBoolean("inBlacklist");
                user.status = item.optString("status");
                user.online = item.optBoolean("online");
                user.mutual = item.optBoolean("mutual");
                user.score = item.optInt("score");
                user.banned = item.optBoolean("banned");
                user.deleted = item.optBoolean("deleted") || user.isEmpty();
                user.bookmarked = item.optBoolean("bookmarked");
            } else {
                user.deleted = true;
                user.uid = userId;
            }
        } catch (Exception e) {
            Debug.error("Wrong response parsing", e);
        }
        return user;
    }

    @Override
    public boolean isEditor() {
        return false;
    }
}
