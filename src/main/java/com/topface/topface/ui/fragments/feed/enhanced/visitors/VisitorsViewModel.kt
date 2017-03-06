package com.topface.topface.ui.fragments.feed.enhanced.visitors

import android.content.Context
import com.topface.topface.data.CountersData
import com.topface.topface.data.Visitor
import com.topface.topface.requests.FeedRequest
import com.topface.topface.ui.fragments.feed.enhanced.base.BaseFeedFragmentModel
import com.topface.topface.utils.config.FeedsCache
import com.topface.topface.utils.gcmutils.GCMUtils

class VisitorsViewModel(context: Context) : BaseFeedFragmentModel<Visitor>(context) {

    override val feedsType: FeedsCache.FEEDS_TYPE
        get() = FeedsCache.FEEDS_TYPE.DATA_VISITORS_FEEDS
    override val itemClass: Class<Visitor>
        get() = Visitor::class.java
    override val service: FeedRequest.FeedService
        get() = FeedRequest.FeedService.VISITORS
    override val gcmType: Array<Int>
        get() = arrayOf(GCMUtils.GCM_TYPE_GUESTS)
    override val isForPremium: Boolean
        get() = true
    override val gcmTypeUpdateAction: String?
        get() = GCMUtils.GCM_GUESTS_UPDATE

    override fun isCountersChanged(newCounters: CountersData, currentCounters: CountersData) =
            newCounters.visitors > currentCounters.visitors

    override fun considerDuplicates(first: Visitor, second: Visitor) = first.user?.id == second.user?.id
}