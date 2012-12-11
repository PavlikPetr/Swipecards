package com.topface.topface.data;

import org.json.JSONObject;

public class FeedUser extends AbstractData {
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
    public boolean online; //
    /**
     * Объект города пользователя
     */
    public City city;
    /**
     * Объект основного фото пользователя
     */
    public Photo photo;

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
    }

    public String getNameAndAge() {
        String result;
        if (first_name != null && first_name.length() > 0 && age > 0) {
            result = String.format("%s, %d", first_name, age);
        } else {
            result = first_name;
        }
        return result;
    }
}
