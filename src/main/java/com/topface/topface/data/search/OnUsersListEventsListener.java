package com.topface.topface.data.search;

import com.topface.topface.data.FeedUser;

/**
 * Интерфейс для листенера события окончания списка
 */
public interface OnUsersListEventsListener<T extends FeedUser> {

    /**
     * Событие возникает когда кончается список пользователей (т.е. getCurrentUser вернул null)
     *
     * @param usersList поиск, где кончились пользователи
     */
    void onEmptyList(UsersList<T> usersList);

    /**
     * Событие возникает когда осталось мало пользователей в поиске и нужно загрузить еще
     *
     * @param usersList поиск куда нужно добавить загруженных пользователей
     */
    void onPreload(UsersList<T> usersList);

}
