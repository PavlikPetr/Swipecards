package com.topface.topface.ui.fragments.feed.enhanced.tabbed_likes.mutual

import com.topface.topface.api.FeedRequestFactory
import com.topface.topface.api.IApi
import com.topface.topface.data.CountersData
import com.topface.topface.ui.fragments.feed.dialogs.PopupMenuFragment
import com.topface.topface.ui.fragments.feed.enhanced.tabbed_likes.BaseSympathyFeedFragmentViewModel
import com.topface.topface.utils.config.FeedsCache

class MutualViewModel(val api: IApi) : BaseSympathyFeedFragmentViewModel(api) {

    override val sympathyTypeViewModelType: Long
        get() = PopupMenuFragment.MUTUAL_TYPE

    override val feedsType: FeedsCache.FEEDS_TYPE
        get() = FeedsCache.FEEDS_TYPE.DATA_MUTUALS_FEEDS

    override val service: FeedRequestFactory.FeedService
        get() = FeedRequestFactory.FeedService.MUTUAL

    override fun isCountersChanged(newCounters: CountersData, currentCounters: CountersData) =
            newCounters.mutual > currentCounters.mutual

}