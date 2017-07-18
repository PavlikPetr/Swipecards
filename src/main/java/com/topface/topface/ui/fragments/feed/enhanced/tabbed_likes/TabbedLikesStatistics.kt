package com.topface.topface.ui.fragments.feed.enhanced.tabbed_likes

import com.topface.statistics.processor.annotations.GenerateStatistics
import com.topface.statistics.processor.annotations.SendNow

/**
 * Ключи для отправки статистики экрана Симпатий
 *
 */
@GenerateStatistics
object TabbedLikesStatistics {

    const val TAB_DESIGN_TYPE = "design_type"
    const val TAB_TYPE = "val"

    /**
     * показ экрана лайков
     */
    @SendNow(withSlices = true)
    const val TABBED_LIKES_SHOW = "tabbed_likes_show"

    /**
     *  событие показа определенного таба
     */
    @SendNow(withSlices = true)
    const val TAB_SHOW = "tab_show"
}