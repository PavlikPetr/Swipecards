package com.topface.topface.data;

import com.topface.topface.utils.Debug;
import org.json.JSONException;
import org.json.JSONObject;

/**
* Created with IntelliJ IDEA.
* User: gildor
* Date: 17.09.12
* Time: 14:09
* To change this template use File | Settings | File Templates.
*/
public class Visitor extends AbstractData {
    public int id;
    public String name;
    public int age;
    public boolean online;
    public int time;
    public City city;
    public String avatar_big;
    public String avatar_small;

    public static Visitor parseVisitor(JSONObject object) {
        Visitor visitor = new Visitor();
        try {
            visitor.id = object.getInt("id");
            visitor.name = object.getString("name");
            visitor.time = object.getInt("time");
            visitor.online = object.getBoolean("online");
            visitor.city = City.parseCity(object.getJSONObject("city"));
            visitor.age = object.getInt("age");
            JSONObject avatars = object.getJSONObject("avatars");
            visitor.avatar_big = avatars.getString("big");
            visitor.avatar_small = avatars.getString("small");
        } catch (JSONException e) {
            Debug.error(e);
        }

        return visitor;
    }

    public String getBigLink() {
        return avatar_big;
    }

    public String getSmallLink() {
        return avatar_small;
    }
}
