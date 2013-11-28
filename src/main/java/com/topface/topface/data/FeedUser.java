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
    public Photos photos;
    public int photosCount;
    /**
     * флаг премиум
     */
    public boolean premium;

    public boolean banned;

    public boolean deleted;

    public boolean bookmarked;

    public boolean blocked;

    // соответствующий пользователю элемент списка, может быть null
    // optional(for example for closings fragments)
    public String feedItemId;

    public FeedUser(JSONObject user) {
        if (user != null) {
            fillData(user);
        }
    }

    public FeedUser(JSONObject user, FeedItem item) {
        this(user);
        feedItemId = item.id;
    }

    public void fillData(JSONObject user) {
        this.id = user.optInt("id");

        this.first_name = user.optString("firstName");
        this.age = user.optInt("age");
        this.online = user.optBoolean("online");
        this.city = new City(user.optJSONObject("city"));
        this.photo = new Photo(user.optJSONObject("photo"));
        this.sex = user.optInt("sex");
        this.premium = user.optBoolean("premium");
        this.banned = user.optBoolean("banned");
        this.deleted = user.optBoolean("deleted") || this.isEmpty();
        this.bookmarked = user.optBoolean("bookmarked");
        this.blocked = user.optBoolean("inBlacklist");
        if (user.has("photos")) {
            this.photos = new Photos(user.optJSONArray("photos"));
        } else {
            this.photos = new Photos();
            this.photos.add(this.photo);
        }
        this.photosCount = user.optInt("photosCount", photos.size());
        this.feedItemId = user.optString("feedItemId");
    }

    public String getNameAndAge() {
        String result;
        if (!TextUtils.isEmpty(first_name) && age > 0) {
            result = first_name + ", " + age;
        } else {
            result = first_name;
        }
        return result;
    }

    @Override
    public JSONObject toJson() throws JSONException {
        JSONObject json = new JSONObject();//TODO do not serialize twice for closings in FeedUser in FeedItem
        json.put("id", id);
        json.put("firstName", first_name);
        json.put("sex", sex);
        json.put("age", age);
        json.put("online", online);
        json.put("city", city.toJson());
        json.put("photo", photo.toJson());
        json.put("premium", premium);
        json.put("bookmarked", bookmarked);
        json.put("inBlacklist", blocked);
        json.put("photos", photos.toJson());
        json.put("feedItemId", feedItemId);
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

    public boolean isEmpty() {
        return id <= 0;
    }

}
