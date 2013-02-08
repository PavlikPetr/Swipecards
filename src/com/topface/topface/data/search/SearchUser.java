package com.topface.topface.data.search;

import com.topface.topface.data.FeedUser;
import com.topface.topface.data.Photos;
import com.topface.topface.data.SerializableToJson;
import org.json.JSONException;
import org.json.JSONObject;

public class SearchUser extends FeedUser implements SerializableToJson {
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
    public int photosCount;

    public SearchUser(JSONObject user) {
        super(user);
    }

    @Override
    public void fillData(JSONObject user) {
        super.fillData(user);

        status = user.optString("status");
        mutual = user.optBoolean("mailmutual");
        photos = new Photos(user.optJSONArray("photos"));
        photosCount = user.optInt("photos_count", photos.size());
    }

    @Override
    public JSONObject toJson() throws JSONException {
        JSONObject json = super.toJson();
        json.put("status", status);
        json.put("mailmutual", mutual);
        json.put("photos", photos.toJson());
        return json;
    }
}
