package com.topface.topface.ui.fragments.feed.enhanced.tabbed_likes

import com.topface.statistics.generated.TabbedLikesStatisticsGeneratedStatistics
import com.topface.topface.R
import com.topface.topface.data.CountersData
import com.topface.topface.ui.fragments.feed.TabbedFeedFragment
import com.topface.topface.ui.fragments.feed.enhanced.tabbed_likes.admiration.AdmirationFragment
import com.topface.topface.ui.fragments.feed.enhanced.tabbed_likes.mutual.MutualFragment
import com.topface.topface.ui.views.toolbar.utils.ToolbarManager
import com.topface.topface.ui.views.toolbar.utils.ToolbarSettingsData
import com.topface.topface.utils.extensions.getString

/**
 * Created by ppavlik on 12.07.17.
 * Экран с табами для симпатий, взаимных, восхищений
 */
class TabbedLikesFragment : TabbedFeedFragment() {

    init {
        TabbedLikesStatisticsGeneratedStatistics.sendNow_NEW_TABBED_LIKES_SHOW()
    }

    override fun onBeforeCountersUpdate(countersData: CountersData?) {
        updatePageCounter(MutualFragment::class.java.name, countersData?.mutual ?: 0)
        updatePageCounter(AdmirationFragment::class.java.name, countersData?.admirations ?: 0)
    }

    override fun addPages() {
        addBodyPage(MutualFragment::class.java.name, R.string.general_mutual.getString(), mCountersData.mutual)
        addBodyPage(AdmirationFragment::class.java.name, R.string.general_admirations.getString(), mCountersData.admirations)
    }

    override fun getLastOpenedPage() = TabbedFeedFragment.mLikesLastOpenedPage

    override fun setLastOpenedPage(lastOpenedPage: Int) {
        TabbedFeedFragment.mLikesLastOpenedPage = lastOpenedPage
    }

    override fun onResume() {
        super.onResume()
        ToolbarManager.setToolbarSettings(ToolbarSettingsData(getString(R.string.general_sympathies)))
    }
}