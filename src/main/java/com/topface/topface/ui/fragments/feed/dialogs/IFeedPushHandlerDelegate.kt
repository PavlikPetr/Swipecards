package com.topface.topface.ui.fragments.feed.dialogs

/**
 * Интерфейс для делегирования дозагрузки списка диалогов, взаимных симпатий и восхищений
 * Created by siberia87 on 01.12.16.
 */
interface IFeedPushHandlerDelegate {
    fun updateFeedDialogs()
    fun updateFeedMutual()
    fun updateFeedAdmiration()
    fun makeItemReadWithFeedId(itemId: String)
    fun makeItemReadUserId(userId: Int, readMessages: Int)
}