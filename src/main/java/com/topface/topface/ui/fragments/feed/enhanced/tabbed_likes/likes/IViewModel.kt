package com.topface.topface.ui.fragments.feed.enhanced.tabbed_likes.likes

/**
 * Created by ppavlik on 24.07.17.
 * Интерфейс для работы со вью-моделью
 */
interface IViewModel<in T : Any> {
    fun update(data: T)
}