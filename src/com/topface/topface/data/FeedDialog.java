package com.topface.topface.data;

import org.json.JSONObject;

public class FeedDialog extends FeedLike {
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


    public static final int OUTPUT_USER_MESSAGE = 0;
    public static final int INPUT_FRIEND_MESSAGE = 1;

    public FeedDialog(JSONObject data) {
        super(data);
    }

    @Override
    public void fillData(JSONObject item) {
        super.fillData(item);
        text = item.optString("text");
        link = item.optString("link");
        if (type == MAP || type == ADDRESS) {
            geo = new Geo(item);
        }
    }
}
