package com.topface.topface.ui.fragments.feed.enhanced.tabbed_likes

import com.topface.topface.data.FeedItem

/**
 * Модели для симпатий-взаимных-восхощений
 */

// Класс для заглушки "нет симпатий" и "ты не вип"
class NoSympNoVipStub

// Класс для заглушки "нет симпатий", но пользователь ВИП
class NoSympButVipStub

// Класс для заглушки "нет взаимных симпатий"
class NoMutualsStub

// Лоадер для взаимных и васхищений
data class LoaderStub(var plc: String)

// Класс для заглушки "нет восхищений"
class NoAdmirationsStub

//события из попапМеню, "Удаление фида"
data class PopupMenuDeleteEvent(val feedForDelete: FeedItem, val menuPopupType: Long) : IMenuPopupEvent {
    override fun getItemForAction() = feedForDelete
    override fun getPopupType() = menuPopupType

}

//события из попапМеню, "Добавление в черный список"
data class PopupMenuAddToBlackListEvent(val feedForBlackList: FeedItem, val menuPopupType: Long) : IMenuPopupEvent {
    override fun getItemForAction() = feedForBlackList
    override fun getPopupType() = menuPopupType

}

interface IMenuPopupEvent {
    fun getPopupType(): Long
    fun getItemForAction(): FeedItem
}