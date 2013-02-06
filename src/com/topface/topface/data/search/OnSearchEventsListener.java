package com.topface.topface.data.search;

/**
 * Интерфейс для листенера события окончания списка
 */
public interface OnSearchEventsListener {

    /**
     * Событие возникает когда кончается список пользователей (т.е. getCurrentUser вернул null)
     *
     * @param search поиск, где кончились пользователи
     */
    public void onEmptyList(Search search);

    /**
     * Событие возникает когда осталось мало пользователей в поиске и нужно загрузить еще
     *
     * @param search поиск куда нужно добавить загруженных пользователей
     */
    public void onPreload(Search search);

}
