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
public class Visitor extends AbstractFeedItem {

    public static Visitor parse(JSONObject object) {
        Visitor visitor = new Visitor();
        try {
            visitor.uid = object.getInt("id");
            visitor.first_name = object.getString("name");
            visitor.created = object.getInt("time") * 1000;
            visitor.online = object.getBoolean("online");
            // city
            JSONObject city = object.getJSONObject("city");
            visitor.city_id = city.optInt("id");
            visitor.city_name = city.optString("name");
            visitor.city_full = city.optString("full");
            visitor.age = object.getInt("age");

            initPhotos(object, visitor);

        } catch (JSONException e) {
            Debug.error(e);
        }

        return visitor;
    }
}
