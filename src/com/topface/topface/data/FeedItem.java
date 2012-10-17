package com.topface.topface.data;

import com.topface.topface.ui.adapters.FeedList;
import com.topface.topface.ui.adapters.IListLoader;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Абстрактный класс, реализующий основные поля и возможности элеметнов ленты (Диалоги, Лайки, Симпатии)
 */
public class FeedItem extends AbstractData implements IListLoader {

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
     * имеются ли в ленте ещё элементы для пользователя
     */
    public static boolean more;
    /**
     * Пользователь (автор) элемента списка
     */
    public FeedUser user;

    //Loader indicators
    private boolean isListLoader = false;
    private boolean isListLoaderRetry = false;

    public FeedItem(JSONObject data) {
        super(data);
    }

    public void fillData(JSONObject item) {
        this.type = item.optInt("type");
        this.id = item.optInt("id");
        this.created = item.optLong("created") * 1000;
        this.target = item.optInt("target");
        this.unread = item.optBoolean("unread");
        this.user = new FeedUser(item.optJSONObject("user"));
    }


    public FeedItem(IListLoader.ItemType type) {
        switch (type) {
            case LOADER:
                isListLoader = true;
                break;
            case RETRY:
                isListLoaderRetry = true;
                break;
            case NONE:
            default:
                isListLoader = false;
                isListLoaderRetry = false;
                break;
        }
    }

    @Override
    public boolean isLoader() {
        return isListLoader;
    }

    @Override
    public boolean isLoaderRetry() {
        return isListLoaderRetry;
    }

    public static <T extends FeedItem> FeedList<T> getList(JSONArray list) {
        return null;
    }

}
