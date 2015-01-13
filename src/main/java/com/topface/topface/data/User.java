package com.topface.topface.data;

import com.topface.framework.utils.Debug;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.utils.CacheProfile;

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

    public User(int userId, ApiResponse response) {
        super(response);
        uid = userId;
    }

    public User() {
        super();
    }

    @Override
    protected void fillData(JSONObject resp) {
        try {
            if (resp != null) {
                super.fillData(resp);
                platform = resp.optString("platform");
                lastVisit = resp.optInt("lastVisit");
                inBlackList = resp.optBoolean("inBlacklist");
                status = resp.optString("status");
                online = resp.optBoolean("online");
                mutual = resp.optBoolean("mutual");
                score = resp.optInt("score");
                banned = resp.optBoolean("banned");
                deleted = resp.optBoolean("deleted") || isEmpty();
                bookmarked = resp.optBoolean("bookmarked");
                isSympathySent = resp.optBoolean("isSympathySent");
                if (CacheProfile.isEditor()) {
                    socialInfo = UserSocialInfo.parse(resp.optString("info"));
                }
            } else {
                deleted = true;
            }
        } catch (Exception e) {
            Debug.error("User parse exception", e);
        }
    }

    @Override
    public boolean isEditor() {
        return false;
    }
}
