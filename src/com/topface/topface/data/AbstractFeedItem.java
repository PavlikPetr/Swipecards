package com.topface.topface.data;

import com.topface.topface.ui.adapters.IListLoader;

/**
 * Абстрактный класс, реализующий основные поля и возможности элеметнов ленты (Диалоги, Лайки, Симпатии)
 */
abstract public class AbstractFeedItem extends AbstractDataWithPhotos implements IListLoader {
    // Data
    public static int unread_count; // общее количество непрочитанных диалогов
    public static boolean more;     // имеются ли в ленте ещё элементы для пользователя

    public int type; // идентификатор типа сообщения диалога
    public int id; // идентификатор события в ленте
    public int uid; // идентификатор отправителя
    public long created; // таймстамп отправления события
    public int target; // направление события в ленте. Возможные занчения: 0 - для исходящего события, 1 - для входящего события
    public boolean unread; // флаг причитанного диалога
    public String first_name; // имя отправителя в текущей локали
    public int sex; // имя отправителя в текущей локали
    public int age; // возраст отправителя
    public boolean online; // флаг нахождения отправителя онлайн
    public int city_id; // идентификатор города
    public String city_name; // наименование города в локали указанной при авторизации
    public String city_full; // полное наименование города с указанием региона, если он определен. Отдается в локали пользователя, указанной при авторизации

    public String text;  // текст сообщения, если type = MESSAGE

    //Loader indicators
    private boolean isListLoader = false;
    private boolean isListLoaderRetry = false;

    public AbstractFeedItem() {
        this(ItemType.NONE);
    }

    public AbstractFeedItem(IListLoader.ItemType type) {
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

    public int getUid() {
        return uid;
    }

    @Override
    public boolean isLoader() {
        return isListLoader;
    }

    @Override
    public boolean isLoaderRetry() {
        return isListLoaderRetry;
    }

    @Override
    public void switchToLoader() {
        isListLoader = false;
        isListLoaderRetry = true;
    }
}
