package com.topface.topface.statistics

import com.topface.statistics.processor.annotations.GenerateStatistics
import com.topface.statistics.processor.annotations.SendNow
import com.topface.topface.App

/**
 * Ключи для отправки статистики "Люди рядом"
 * Created by ppavlik on 26.12.16.
 */

@GenerateStatistics
object PeopleNearbyStatistics {

    /**
     * Уникальная статистика показа экрана пермишинов
     */
    @SendNow(single = false, unique = true)
    const val PEOPLE_NEARBY_PERMISSION_OPEN = "mobile_nearby_permission_open"

    /**
     * Уникальная статистика показа разблокированного (с разрешенными пермишинами) экрана "Люди рядом"
     */
    @SendNow(single = false, unique = true)
    const val PEOPLE_NEARBY_OPEN = "mobile_nearby_open"

    /**
     * Уникальный статистика пятого показа экрана "Люди рядом"
     */
    @SendNow(single = false, unique = true)
    const val PEOPLE_NEARBY_FIFTH_OPEN = "mobile_nearby_fifth_open"

    /**
     * Показ экрана "Люди рядом" с разрешенным доступом к гео
     */
    @SendNow(single = false)
    const val PEOPLE_NOT_UNIQUE_NEARBY_OPEN = "mobile_not_unique_nearby_open"
}