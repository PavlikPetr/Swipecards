package com.topface.topface.data;

import com.topface.framework.utils.Debug;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.utils.CacheProfile;

import org.json.JSONException;
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
    public boolean isSympathySent;
    public UserSocialInfo socialInfo;   // info about social network

    public static User parse(int userId, ApiResponse response) {
        return parse(userId, response.getJsonResult());
    }

    public static User parse(int userId, String userProfileJson) {
        try {
            return parse(userId, new JSONObject(userProfileJson));
        } catch (JSONException e) {
            Debug.error("Wrong response parsing", e);
        }
        return new User();
    }

    public static User parse(int userId, JSONObject userProfileJson) {
        User user = new User();

        try {
            if (userProfileJson != null) {
                parse(user, userProfileJson);
                user.platform = userProfileJson.optString("platform");
                user.lastVisit = userProfileJson.optInt("lastVisit");
                user.inBlackList = userProfileJson.optBoolean("inBlacklist");
                user.status = userProfileJson.optString("status");
                user.online = userProfileJson.optBoolean("online");
                user.mutual = userProfileJson.optBoolean("mutual");
                user.score = userProfileJson.optInt("score");
                user.banned = userProfileJson.optBoolean("banned");
                user.deleted = userProfileJson.optBoolean("deleted") || user.isEmpty();
                user.bookmarked = userProfileJson.optBoolean("bookmarked");
                user.isSympathySent = userProfileJson.optBoolean("isSympathySent");
                if (CacheProfile.isEditor()) {
                    user.socialInfo = UserSocialInfo.parse(userProfileJson.optString("info"));
                }
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
