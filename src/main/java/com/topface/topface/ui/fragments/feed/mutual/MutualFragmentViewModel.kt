package com.topface.topface.ui.fragments.feed.mutual

import com.topface.topface.data.CountersData
import com.topface.topface.data.FeedMutual
import com.topface.topface.databinding.FragmentFeedBaseBinding
import com.topface.topface.requests.FeedRequest
import com.topface.topface.ui.fragments.feed.feed_api.FeedApi
import com.topface.topface.ui.fragments.feed.feed_base.BaseFeedFragmentViewModel
import com.topface.topface.ui.fragments.feed.feed_base.IFeedNavigator
import com.topface.topface.utils.config.FeedsCache
import com.topface.topface.utils.gcmutils.GCMUtils

/**
 * VM для фрагмента взаимных лайков
 * Created by tiberal on 22.08.16.
 */
class MutualFragmentViewModel(binding: FragmentFeedBaseBinding, navigator: IFeedNavigator, api: FeedApi) :
        BaseFeedFragmentViewModel<FeedMutual>(binding, navigator, api) {
    override fun isCountersChanged(newCounters: CountersData, currentCounters: CountersData): Boolean {
        return newCounters.mutual > currentCounters.mutual
    }

    override val gcmType: Array<Int>
        get() = arrayOf(GCMUtils.GCM_TYPE_MUTUAL)

    override val gcmTypeUpdateAction: String?
        get() = GCMUtils.GCM_MUTUAL_UPDATE

    override val service: FeedRequest.FeedService
        get() = FeedRequest.FeedService.MUTUAL

    override val itemClass: Class<FeedMutual>
        get() = FeedMutual::class.java

    override val feedsType: FeedsCache.FEEDS_TYPE
        get() = FeedsCache.FEEDS_TYPE.DATA_MUTUALS_FEEDS

    override fun onRefreshed() {
        mAdapter?.let {
            it.data.forEachIndexed { position, feedMutual ->
                if (feedMutual.unread) {
                    feedMutual.unread = false
                    it.notifyItemChanged(position)
                }
            }
        }
    }

}