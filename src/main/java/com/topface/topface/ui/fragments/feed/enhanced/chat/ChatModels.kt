package com.topface.topface.ui.fragments.feed.enhanced.chat

import com.topface.topface.data.History

/**
 * Модельки для чата
 */

/**
 * Лоадер на время отправки запроса
 */
data class ChatLoader(var diffTemp: Int = 0)

/**
 * Событие на удаление
 */
data class ChatDeleteEvent(var itemPosition: Int = 0)

/**
 * Событие на жалобу
 */
data class ChatComplainEvent(var itemPosition: Int = 0)
