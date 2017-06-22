package com.topface.topface.statistics

import com.topface.statistics.processor.annotations.GenerateStatistics
import com.topface.statistics.processor.annotations.SendNow

/**
 * Ключи для отправки статистики показов/скрытий progressBar
 * Created by ppavlik on 24.03.17.
 */

@GenerateStatistics
object ProgressStatistics {

    /**
     * Показ лоадера на экране
     */
    @SendNow(single = false, withSlices = true)
    const val LOADER_SHOW = "mobile_loader_start"

    /**
     * Скрытие лоадера с экрана
     */
    @SendNow(single = false, withSlices = true)
    const val LOADER_HIDE = "mobile_loader_stop"
}