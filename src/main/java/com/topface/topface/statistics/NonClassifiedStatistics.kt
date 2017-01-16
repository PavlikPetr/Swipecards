package com.topface.topface.statistics

import com.topface.statistics.processor.annotations.GenerateStatistics
import com.topface.statistics.processor.annotations.SendNow
import com.topface.topface.App

/**
 * Ключи для отправки различных событий статистики
 * Created by ppavlik on 26.12.16.
 */

@GenerateStatistics
object NonClassifiedStatistics {

    /**
     * Статистика показов чужого профиля
     */
    @SendNow(single = false, withSlices = true)
    const val PROFILE_OPEN = "mobile_profile_open"

    /**
     * Статистика просмотров экрана блокировки отправки сообщения без взаимной симпатии
     */
    @SendNow(single = false, withSlices = true)
    const val DIALOGS_BLOCK_MESSAGES_WITHOUT_MUTUAL = "mobile_dialogs_block_messages_without_mutual"
}