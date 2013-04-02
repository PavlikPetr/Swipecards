package com.topface.topface.data;

import android.text.TextUtils;
import org.json.JSONException;
import org.json.JSONObject;

public class FeedUser extends AbstractData implements SerializableToJson {
    /**
     * идентификатор отправителя
     */
    public int id;
    /**
     * имя отправителя в текущей локали
     */
    public String first_name;
    /**
     * имя отправителя в текущей локали
     */
    public int sex;
    /**
     * возраст отправителя
     */
    public int age;
    /**
     * флаг нахождения отправителя онлайн
     */
    public boolean online;
    /**
     * Объект города пользователя
     */
    public City city;
    /**
     * Объект основного фото пользователя
     */
    public Photo photo;
    /**
     * флаг премиум
     */
    public boolean premium;

    public boolean banned;

    public boolean deleted;

    public FeedUser(JSONObject user) {
        super(user);
    }

    public void fillData(JSONObject user) {
        this.id = user.optInt("id");
        this.first_name = user.optString("first_name");
        this.age = user.optInt("age");
        this.online = user.optBoolean("online");
        this.city = new City(user.optJSONObject("city"));
        this.photo = new Photo(user.optJSONObject("photo"));
        this.sex = user.optInt("sex");
        this.premium = user.optBoolean("premium");
        this.banned = user.optBoolean("banned");
        this.deleted = user.optBoolean("deleted");
    }

    public String getNameAndAge() {
        String result;
        if (!TextUtils.isEmpty(first_name) && age > 0) {
            result = String.format("%s, %d", first_name, age);
        } else {
            result = first_name;
        }
        return result;
    }

    @Override
    public JSONObject toJson() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("id", id);
        json.put("first_name", first_name);
        json.put("sex", sex);
        json.put("age", age);
        json.put("online", online);
        json.put("city", city.toJson());
        json.put("photo", photo.toJson());
        json.put("premium", premium);

        return json;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof FeedUser) {
            return ((FeedUser) o).id == id;
        } else {
            return super.equals(o);
        }
    }
}
