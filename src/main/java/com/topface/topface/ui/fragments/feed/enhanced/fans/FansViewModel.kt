package com.topface.topface.ui.fragments.feed.enhanced.fans

import android.content.Context
import com.topface.topface.api.FeedRequestFactory
import com.topface.topface.api.IApi
import com.topface.topface.api.responses.FeedBookmark
import com.topface.topface.api.responses.GetFeedBookmarkListResponse
import com.topface.topface.api.responses.IBaseFeedResponse
import com.topface.topface.data.CountersData
import com.topface.topface.ui.fragments.feed.enhanced.base.BaseFeedFragmentModel
import com.topface.topface.utils.config.FeedsCache
import com.topface.topface.utils.gcmutils.GCMUtils

class FansViewModel(context: Context, api: IApi) : BaseFeedFragmentModel<FeedBookmark>(context, api) {
    override val responseClass: Class<out IBaseFeedResponse>
        get() = GetFeedBookmarkListResponse::class.java
    override val feedsType: FeedsCache.FEEDS_TYPE
        get() = FeedsCache.FEEDS_TYPE.DATA_FANS_FEEDS
    override val itemClass: Class<FeedBookmark>
        get() = FeedBookmark::class.java
    override val service: FeedRequestFactory.FeedService
        get() = FeedRequestFactory.FeedService.FANS
    override val gcmType: Array<Int>
        get() = arrayOf(GCMUtils.GCM_TYPE_UNKNOWN)
    override val isForPremium: Boolean
        get() = true

    override fun isCountersChanged(newCounters: CountersData, currentCounters: CountersData) =
            newCounters.fans > currentCounters.fans

    override fun considerDuplicates(first: FeedBookmark, second: FeedBookmark) =
            first.user?.id == second.user?.id
}