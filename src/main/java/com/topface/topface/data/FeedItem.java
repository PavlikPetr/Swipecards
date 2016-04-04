package com.topface.topface.data;

import android.os.Parcel;
import android.os.Parcelable;

import com.topface.framework.JsonUtils;
import com.topface.framework.utils.Debug;
import com.topface.topface.utils.DateUtils;
import com.topface.topface.utils.ad.NativeAd;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Абстрактный класс, реализующий основные поля и возможности элеметнов ленты (Диалоги, Лайки, Симпатии)
 */
abstract public class FeedItem extends LoaderData implements Parcelable {

    public static final String NULL_USER = "null_user";

    /**
     * идентификатор события в ленте
     */
    public String id;
    /**
     * идентификатор типа сообщения диалога
     */
    public int type;
    /**
     * таймстамп отправления события
     */
    public long created;
    /**
     * строка с временем отправки события (поле created) относительным времени пользователь (вчера, неделю назад и т.п)
     * ВАЖНО! для ускорения скорости работы парсится только внутри потомков
     */
    public String createdRelative;
    /**
     * направление события в ленте. Возможные занчения: 0 - для исходящего события, 1 - для входящего события
     */
    public int target;
    /**
     * флаг причитанного элемента
     */
    public boolean unread;
    /**
     * Счетчик непрочитанных сообщений
     */
    public int unreadCounter;
    /**
     * Пользователь (автор) элемента списка
     */
    public FeedUser user;

    private NativeAd mNativeAd;

    public FeedItem() {
        super(ItemType.NONE);
    }

    public FeedItem(JSONObject data) {
        super(ItemType.NONE);
        if (data != null) {
            fillData(data);
        }
    }

    public FeedItem(NativeAd nativeAd) {
        this();
        mNativeAd = nativeAd;
    }

    public FeedItem(Parcel in) {
        super(in);
        id = in.readString();
        type = in.readInt();
        created = in.readLong();
        createdRelative = in.readString();
        target = in.readInt();
        unread = in.readByte() == 1;
        unreadCounter = in.readInt();
        String usr = in.readString();
        if (!usr.equals(NULL_USER)) {
            user = JsonUtils.fromJson(usr, FeedUser.class);
        }
        mNativeAd = in.readParcelable(NativeAd.class.getClassLoader());
    }

    public FeedItem(ItemType type) {
        super(type);
    }

    public void fillData(JSONObject item) {
        this.type = item.optInt("type");
        Object testId = item.opt("id");

        if (testId instanceof Integer) {
            id = Integer.toString((Integer) testId);
        } else {
            id = (String) testId;
        }

        this.created = item.optLong("created") * 1000;
        this.target = item.optInt("target");
        this.unread = item.optBoolean("unread");
        this.unreadCounter = item.optInt("unreadCount");
        JSONObject json = item.optJSONObject("user");
        if (json != null) {
            this.user = JsonUtils.fromJson(json.toString(), FeedUser.class);
            this.user.setFeedItemId(id);
        }
    }

    /**
     * Методе для форматирования относительного времени создания элемента для показа в ленте
     */
    protected String getRelativeCreatedDate(long date) {
        return DateUtils.getRelativeDate(date, true);
    }

    public boolean isAd() {
        return mNativeAd != null;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(id);
        dest.writeInt(type);
        dest.writeLong(created);
        dest.writeString(createdRelative);
        dest.writeInt(target);
        dest.writeByte((byte) (unread ? 1 : 0));
        dest.writeInt(unreadCounter);
        if (user != null) {
            try {
                dest.writeString(user.toJson().toString());
            } catch (JSONException e) {
                Debug.error(e);
            }
        } else {
            dest.writeString(NULL_USER);
        }
        dest.writeParcelable(mNativeAd, flags);
    }

    @SuppressWarnings("SimplifiableIfStatement")
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof FeedItem)) return false;
        if (!super.equals(o)) return false;
        FeedItem feedItem = (FeedItem) o;
        if (type != feedItem.type) return false;
        if (target != feedItem.target) return false;
        if (unread != feedItem.unread) return false;
        if (unreadCounter != feedItem.unreadCounter) return false;
        if (createdRelative != null ? !createdRelative.equals(feedItem.createdRelative) : feedItem.createdRelative != null)
            return false;
        return !(user != null ? !user.equals(feedItem.user) : feedItem.user != null);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (id != null ? id.hashCode() : 0);
        result = 31 * result + type;
        result = 31 * result + (createdRelative != null ? createdRelative.hashCode() : 0);
        result = 31 * result + target;
        result = 31 * result + (unread ? 1 : 0);
        result = 31 * result + unreadCounter;
        result = 31 * result + (user != null ? user.hashCode() : 0);
        result = 31 * result + (mNativeAd != null ? mNativeAd.hashCode() : 0);
        return result;
    }
}
