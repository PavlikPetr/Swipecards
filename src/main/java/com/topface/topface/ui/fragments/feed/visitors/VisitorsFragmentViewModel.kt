package com.topface.topface.ui.fragments.feed.visitors

import android.os.Bundle
import com.topface.topface.data.CountersData
import com.topface.topface.data.FeedDialog
import com.topface.topface.data.FeedListData
import com.topface.topface.data.Visitor
import com.topface.topface.databinding.FragmentFeedBaseBinding
import com.topface.topface.requests.FeedRequest
import com.topface.topface.ui.fragments.feed.feed_api.FeedApi
import com.topface.topface.ui.fragments.feed.feed_base.BaseFeedAdapter
import com.topface.topface.ui.fragments.feed.feed_base.BaseFeedFragmentViewModel
import com.topface.topface.ui.fragments.feed.feed_base.IFeedNavigator
import com.topface.topface.utils.config.FeedsCache
import com.topface.topface.utils.gcmutils.GCMUtils


class VisitorsFragmentViewModel(binding: FragmentFeedBaseBinding, navigator: IFeedNavigator, api: FeedApi) :
        BaseFeedFragmentViewModel<Visitor>(binding, navigator, api) {
    override fun isCountersChanged(newCounters: CountersData, currentCounters: CountersData): Boolean {
        return newCounters.visitors > currentCounters.visitors
    }

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
    override val isNeedReadItems: Boolean
        get() = false

    override fun topFeedsLoaded(data: FeedListData<Visitor>?, requestBundle: Bundle) {
        super.topFeedsLoaded(data, requestBundle)
        binding?.feedList?.adapter?.let {
            if (it is BaseFeedAdapter<*, *>) {
                it.disableAllHighlight()
            }
        }
    }

    override fun considerDuplicates(first: Visitor, second: Visitor) = first.user?.id == second.user?.id
}