package com.topface.topface.data;

import android.os.Parcel;
import android.os.Parcelable;

import com.topface.framework.utils.Debug;
import com.topface.topface.utils.DateUtils;

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

    public FeedItem(JSONObject data) {
        super(ItemType.NONE);
        if (data != null) {
            fillData(data);
        }
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
            try {
                user = new FeedUser(new JSONObject(usr));
            } catch (JSONException e) {
                Debug.error(e);
            }
        }
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
        this.user = new FeedUser(item.optJSONObject("user"), this);
    }

    /**
     * Методе для форматирования относительного времени создания элемента для показа в ленте
     */
    protected String getRelativeCreatedDate(long date) {
        return DateUtils.getRelativeDate(date, true);
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
    }

}
