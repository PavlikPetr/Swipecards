package com.topface.topface.data;

import org.json.JSONException;
import org.json.JSONObject;

public class Leader extends FeedUser {
    private String status;

    public Leader(JSONObject data) {
        super(data);
    }

    @Override
    public void fillData(JSONObject user) {
        super.fillData(user);
        status = user.optString("status");
    }

    @Override
    public JSONObject toJson() throws JSONException {
        JSONObject object = super.toJson();
        object.put("status", status);
        return object;
    }

    public String getStatus() {
        return status;
    }
}
