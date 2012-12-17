package com.topface.topface.data;

import org.json.JSONObject;

/**
 * Абстрактный класс, реализующий основные поля и возможности элеметнов ленты (Диалоги, Лайки, Симпатии)
 */
abstract public class FeedItem extends AbstractLoaderData {

    /**
     * идентификатор события в ленте
     */
    public int id;
    /**
     * идентификатор типа сообщения диалога
     */
    public int type;
    /**
     * таймстамп отправления события
     */
    public long created;
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
     * имеются ли в ленте ещё элементы для пользователя
     */
    public static boolean more;
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

    public FeedItem(ItemType type) {
        super(type);
    }

    public void fillData(JSONObject item) {
        this.type = item.optInt("type");
        this.id = item.optInt("id");
        this.created = item.optLong("created") * 1000;
        this.target = item.optInt("target");
        this.unread = item.optBoolean("unread");
        this.unreadCounter = item.optInt("unreadCount");
        this.user = new FeedUser(item.optJSONObject("user"));
    }
}
