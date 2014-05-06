package com.topface.topface.data.search;

import com.topface.topface.data.FeedUser;
import com.topface.topface.data.Profile;
import com.topface.topface.data.SerializableToJson;

import org.json.JSONException;
import org.json.JSONObject;

public class SearchUser extends FeedUser implements SerializableToJson {
    /**
     * статус пользователя
     */
    protected String status;
    /**
     * флаг возможности отправки взаимной симпатии
     */
    public boolean mutual;

    // Flags
    public boolean skipped;
    public boolean rated;

    public SearchUser(JSONObject user) {
        super(user);
    }

    @Override
    public void fillData(JSONObject user) {
        super.fillData(user);

        status = Profile.normilizeStatus(user.optString("status"));
        mutual = user.optBoolean("isMutualPossible");
        rated = user.optBoolean("rated", false);
    }

    @Override
    public JSONObject toJson() throws JSONException {
        JSONObject json = super.toJson();
        json.put("status", status);
        json.put("mutual", mutual);
        json.put("photos", photos.toJson());
        json.put("photosCount", photosCount);
        json.put("rated", rated);
        return json;
    }

    public String getStatus() {
        return status;
    }
}
