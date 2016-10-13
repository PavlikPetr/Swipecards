package com.topface.topface.ui.fragments.feed.fans

import android.os.Bundle
import com.topface.topface.data.CountersData
import com.topface.topface.data.FeedBookmark
import com.topface.topface.data.FeedDialog
import com.topface.topface.data.FeedListData
import com.topface.topface.databinding.FragmentFeedBaseBinding
import com.topface.topface.requests.FeedRequest
import com.topface.topface.ui.fragments.feed.feed_api.FeedApi
import com.topface.topface.ui.fragments.feed.feed_base.BaseFeedAdapter
import com.topface.topface.ui.fragments.feed.feed_base.BaseFeedFragmentViewModel
import com.topface.topface.ui.fragments.feed.feed_base.IFeedNavigator
import com.topface.topface.utils.config.FeedsCache
import com.topface.topface.utils.gcmutils.GCMUtils


class FansFragmentViewModel(binding: FragmentFeedBaseBinding, navigator: IFeedNavigator, api: FeedApi) :
        BaseFeedFragmentViewModel<FeedBookmark>(binding, navigator, api) {
    override fun isCountersChanged(newCounters: CountersData, currentCounters: CountersData): Boolean {
        return newCounters.fans > currentCounters.fans
    }

    override val typeFeedFragment: String?
        get() = null
    override val feedsType: FeedsCache.FEEDS_TYPE
        get() = FeedsCache.FEEDS_TYPE.DATA_FANS_FEEDS
    override val itemClass: Class<FeedBookmark>
        get() = FeedBookmark::class.java
    override val service: FeedRequest.FeedService
        get() = FeedRequest.FeedService.FANS
    override val gcmType: Array<Int>
        get() = arrayOf(GCMUtils.GCM_TYPE_UNKNOWN)
    override val isForPremium: Boolean
        get() = true
    override val isNeedReadItems: Boolean
        get() = false

    override fun topFeedsLoaded(data: FeedListData<FeedBookmark>?, requestBundle: Bundle) {
        super.topFeedsLoaded(data, requestBundle)
        binding?.feedList?.adapter?.let {
            if (it is BaseFeedAdapter<*, *>) {
                it.disableAllHighlight()
            }
        }
    }

    override fun considerDuplicates(first: FeedBookmark, second: FeedBookmark) = first.user?.id == second.user?.id
}