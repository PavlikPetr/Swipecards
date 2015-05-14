package com.topface.topface.data;

import android.os.Parcel;
import android.os.Parcelable;

import com.topface.topface.Static;
import com.topface.topface.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

public class FeedUser extends AbstractData implements SerializableToJson, Parcelable {
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
    public String status;
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

    public FeedUser(Parcel in) {
        this.id = in.readInt();
        this.first_name = in.readString();
        this.sex = in.readInt();
        this.age = in.readInt();
        this.online = in.readByte() != 0;
        this.city = in.readParcelable(City.class.getClassLoader());
        this.photo = in.readParcelable(Photo.class.getClassLoader());
        Parcelable[] parcelableArray = in.readParcelableArray(Photos.class.getClassLoader());
        this.photos = new Photos();
        if (parcelableArray != null) {
            for (int i = 0; i < parcelableArray.length; i++) {
                this.photos.add(i, (Photo) parcelableArray[i]);
            }
        }
        this.photosCount = in.readInt();
        this.premium = in.readByte() != 0;
        this.banned = in.readByte() != 0;
        this.deleted = in.readByte() != 0;
        this.bookmarked = in.readByte() != 0;
        this.blocked = in.readByte() != 0;
        this.feedItemId = in.readString();
    }

    public void fillData(JSONObject user) {
        this.id = user.optInt("id");

        this.status = user.optString("status");
        this.first_name = Utils.optString(user, "firstName");
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
        return Utils.getNameAndAge(first_name, age);
    }

    @Override
    public JSONObject toJson() throws JSONException {
        JSONObject json = new JSONObject();//TODO do not serialize twice for closings in FeedUser in FeedItem
        json.put("id", id);
        json.put("firstName", first_name);
        json.put("sex", sex);
        json.put("age", age);
        json.put("online", online);
        json.put("city", city != null ? city.toJson() : Static.EMPTY);
        json.put("photo", photo != null ? photo.toJson() : new Photo().toJson());
        json.put("premium", premium);
        json.put("bookmarked", bookmarked);
        json.put("inBlacklist", blocked);
        json.put("photos", photos != null ? photos.toJson() : Static.EMPTY);
        json.put("feedItemId", feedItemId);
        return json;
    }

    @Override
    public void fromJSON(String json) {

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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(first_name);
        dest.writeInt(sex);
        dest.writeInt(age);
        dest.writeByte((byte) (online ? 1 : 0));
        dest.writeParcelable(city, flags);
        dest.writeParcelable(photo, flags);
        dest.writeParcelableArray(photos.toArray(new Photo[photos.size()]), flags);
        dest.writeInt(photosCount);
        dest.writeByte((byte) (premium ? 1 : 0));
        dest.writeByte((byte) (banned ? 1 : 0));
        dest.writeByte((byte) (deleted ? 1 : 0));
        dest.writeByte((byte) (bookmarked ? 1 : 0));
        dest.writeByte((byte) (blocked ? 1 : 0));
        dest.writeString(feedItemId);
    }

    public static final Parcelable.Creator CREATOR =
            new Parcelable.Creator() {
                public FeedUser createFromParcel(Parcel in) {
                    return new FeedUser(in);
                }

                public FeedUser[] newArray(int size) {
                    return new FeedUser[size];
                }
            };

}
