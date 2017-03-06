package com.topface.topface.ui.fragments.feed.enhanced.fans

import android.content.Context
import android.os.Bundle
import com.topface.topface.data.CountersData
import com.topface.topface.data.FeedBookmark
import com.topface.topface.data.FeedListData
import com.topface.topface.requests.FeedRequest
import com.topface.topface.ui.fragments.feed.enhanced.base.BaseFeedFragmentModel
import com.topface.topface.utils.config.FeedsCache
import com.topface.topface.utils.gcmutils.GCMUtils


class FansViewModel(context: Context) : BaseFeedFragmentModel<FeedBookmark>(context) {

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

    override fun isCountersChanged(newCounters: CountersData, currentCounters: CountersData) =
            newCounters.fans > currentCounters.fans

    override fun considerDuplicates(first: FeedBookmark, second: FeedBookmark) =
            first.user?.id == second.user?.id

}