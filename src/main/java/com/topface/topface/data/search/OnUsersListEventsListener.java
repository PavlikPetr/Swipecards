package com.topface.topface.data.search;

/**
 * Интерфейс для листенера события окончания списка
 */
public interface OnUsersListEventsListener {

    /**
     * Событие возникает когда кончается список пользователей (т.е. getCurrentUser вернул null)
     *
     * @param usersList поиск, где кончились пользователи
     */
    public void onEmptyList(UsersList usersList);

    /**
     * Событие возникает когда осталось мало пользователей в поиске и нужно загрузить еще
     *
     * @param usersList поиск куда нужно добавить загруженных пользователей
     */
    public void onPreload(UsersList usersList);

}
