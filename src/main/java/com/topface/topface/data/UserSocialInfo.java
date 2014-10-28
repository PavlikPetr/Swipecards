package com.topface.topface.data;

import com.topface.framework.utils.Debug;

import org.json.JSONException;
import org.json.JSONObject;

public class UserSocialInfo {
    public int id;
    public boolean solvent;
    public boolean banned;
    public String link;

    public static UserSocialInfo parse(String source) {
        UserSocialInfo info = new UserSocialInfo();

        try {
            JSONObject json = new JSONObject(source);
            info.id = json.optInt("social");
            info.solvent = json.optBoolean("solvent");
            info.banned = json.optBoolean("banned");
            info.link = json.optString("link");
        } catch (JSONException e) {
            Debug.error("Can't parse user social info", e);
        }

        return info;
    }
}
