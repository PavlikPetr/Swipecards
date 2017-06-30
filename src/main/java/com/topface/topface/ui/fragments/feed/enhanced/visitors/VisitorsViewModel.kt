package com.topface.topface.ui.fragments.feed.enhanced.visitors

import android.view.View
import com.topface.topface.api.FeedRequestFactory
import com.topface.topface.api.IApi
import com.topface.topface.api.responses.GetVisitorsListResponse
import com.topface.topface.api.responses.Visitor
import com.topface.topface.data.CountersData
import com.topface.topface.ui.fragments.feed.enhanced.base.BaseFeedFragmentModel
import com.topface.topface.utils.config.FeedsCache
import com.topface.topface.utils.gcmutils.GCMUtils

class VisitorsViewModel(api: IApi) : BaseFeedFragmentModel<Visitor>(api) {
    override val responseClass: Class<GetVisitorsListResponse>
        get() = GetVisitorsListResponse::class.java
    override val feedsType: FeedsCache.FEEDS_TYPE
        get() = FeedsCache.FEEDS_TYPE.DATA_VISITORS_FEEDS
    override val itemClass: Class<Visitor>
        get() = Visitor::class.java
    override val service: FeedRequestFactory.FeedService
        get() = FeedRequestFactory.FeedService.VISITORS
    override val gcmType: Array<Int>
        get() = arrayOf(GCMUtils.GCM_TYPE_GUESTS)
    override val isForPremium: Boolean
        get() = true
    override val gcmTypeUpdateAction: String?
        get() = GCMUtils.GCM_GUESTS_UPDATE

    override fun isCountersChanged(newCounters: CountersData, currentCounters: CountersData) =
            newCounters.visitors > currentCounters.visitors

    override fun considerDuplicates(first: Visitor, second: Visitor) = first.user?.id == second.user?.id
    
    override fun itemClick(view: View?, itemPosition: Int, data: Visitor?, from: String) = navigator?.showProfile(data, from)
}