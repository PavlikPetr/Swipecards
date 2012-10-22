package com.topface.topface.data;

import org.json.JSONObject;

public class SearchUser extends FeedUser {
    /**
     * статус пользователя
     */
    public String status;
    /**
     * идентификатор пользователя, который послал симпатию, иначе 0
     */
    public boolean mutual;

    // Flags
    public boolean skipped = false;
    public boolean rated = false;
    public Photos photos;

    public SearchUser(JSONObject user) {
        super(user);
    }

    @Override
    public void fillData(JSONObject user) {
        super.fillData(user);

        status = user.optString("status");
        mutual = user.optBoolean("mutual");
        photos = new Photos(user.optJSONArray("photos"));
    }
}
