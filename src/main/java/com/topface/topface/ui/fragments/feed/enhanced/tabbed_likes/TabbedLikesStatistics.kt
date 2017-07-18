package com.topface.topface.ui.fragments.feed.enhanced.tabbed_likes

import com.topface.statistics.processor.annotations.GenerateStatistics
import com.topface.statistics.processor.annotations.SendNow

/**
 * Ключи для отправки статистики экрана Симпатий
 *
 */
@GenerateStatistics
object TabbedLikesStatistics {

    const val TAB_TYPE = "val"

    /**
     * показ старого экрана лайков
     */
    @SendNow()
    const val OLD_TABBED_LIKES_SHOW = "old_tabbed_likes_show"

    /**
     * показ нового экрана лайков
     */
    @SendNow()
    const val NEW_TABBED_LIKES_SHOW = "new_tabbed_likes_show"

    /**
     *  событие показа определенного таба
     */
    @SendNow(withSlices = true)
    const val TAB_SHOW = "tab_show"
}