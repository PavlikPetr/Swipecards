package com.topface.topface.data;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONObject;

public class FeedDialog extends FeedLike implements Parcelable {
    /**
     * текст сообщения, если type = MESSAGE
     */
    public String text;

    /**
     * идентификатор подарка из ленты
     */
    public int gift;

    /**
     *     * строка ссылки изображения подарка из ленты
     *    
     */
    public String link;
    /**
     * Координаты местоположения
     */
    public Geo geo;

    // Constants
    public static final int DEFAULT = 0; // По-умолчанию. Нигде не используется. Если возникает, наверное, надо что-то сделать
    public static final int PHOTO = 1; // Рекламное уведомление
    public static final int GIFT = 2; // Подарок
    public static final int MESSAGE = 3; // Текстовое сообщение
    public static final int MESSAGE_WISH = 4; // Тайное желание
    public static final int MESSAGE_SEXUALITY = 5; // Оценка сексуальности
    public static final int LIKE = 6; // Событие “понравилось”
    public static final int SYMPHATHY = 7; // Событие “симпатия”
    public static final int MESSAGE_WINK = 8; // подмигивание
    public static final int RATE = 9; // Оценка
    public static final int PROMOTION = 10; // Рекламное сообщение
    public static final int MAP = 12; // Текущее местоположение
    public static final int ADDRESS = 13; // Произвольное место на карте
    public static final int LIKE_REQUEST = 15; // Вирус "Получи 5 лайков"
    public static final int MESSAGE_POPULAR_STAGE_1 = 35; // Первый этап блокировки сообщений от популярного пользователя
    public static final int MESSAGE_POPULAR_STAGE_2 = 36; // Второй этап блокировки сообщений от популярного пользователя


    public static final int OUTPUT_USER_MESSAGE = 0;
    public static final int INPUT_FRIEND_MESSAGE = 1;

    public static final Parcelable.Creator<FeedDialog> CREATOR
            = new Parcelable.Creator<FeedDialog>() {
        public FeedDialog createFromParcel(Parcel in) {
            return new FeedDialog(in);
        }

        public FeedDialog[] newArray(int size) {
            return new FeedDialog[size];
        }
    };

    public FeedDialog(JSONObject data) {
        super(data);
    }

    protected FeedDialog(Parcel in) {
        super(in);
        text = in.readString();
        gift = in.readInt();
        link = in.readString();
        geo = (Geo) in.readParcelable(getClass().getClassLoader());
    }

    @Override
    public void fillData(JSONObject item) {
        super.fillData(item);
        text = item.optString("text");
        link = item.optString("link");
        createdRelative = getRelativeCreatedDate(created);
        if (type == MAP || type == ADDRESS) {
            geo = new Geo(item);
        }
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(text);
        dest.writeInt(gift);
        dest.writeString(link);
        dest.writeParcelable(geo, 0);
    }
}
