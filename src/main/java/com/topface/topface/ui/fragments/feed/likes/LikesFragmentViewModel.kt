package com.topface.topface.ui.fragments.feed.likes

import android.view.View
import com.topface.topface.data.CountersData
import com.topface.topface.data.FeedLike
import com.topface.topface.databinding.FragmentFeedBaseBinding
import com.topface.topface.requests.FeedRequest
import com.topface.topface.requests.ReadLikeRequest
import com.topface.topface.ui.fragments.feed.feed_api.FeedApi
import com.topface.topface.ui.fragments.feed.feed_base.BaseFeedFragmentViewModel
import com.topface.topface.ui.fragments.feed.feed_base.IFeedNavigator
import com.topface.topface.ui.fragments.feed.feed_utils.getUserId
import com.topface.topface.utils.config.FeedsCache
import com.topface.topface.utils.gcmutils.GCMUtils

/**
 * VM для фрагмента лайков
 * Created by tiberal on 08.08.16.
 */
class LikesFragmentViewModel(binding: FragmentFeedBaseBinding, navigator: IFeedNavigator, api: FeedApi) :
        BaseFeedFragmentViewModel<FeedLike>(binding, navigator, api) {

    private val TYPE_FEED_FRAGMENT = "like"

    override fun isCountersChanged(newCounters: CountersData, currentCounters: CountersData) =
            newCounters.likes > currentCounters.likes


    override val typeFeedFragment: String?
        get() = TYPE_FEED_FRAGMENT

    override val gcmType: Array<Int>
        get() = arrayOf(GCMUtils.GCM_TYPE_LIKE)

    override val gcmTypeUpdateAction: String?
        get() = GCMUtils.GCM_LIKE_UPDATE

    override val isNeedReadItems: Boolean
        get() = true

    override val service: FeedRequest.FeedService
        get() = FeedRequest.FeedService.LIKES

    override val itemClass: Class<FeedLike>
        get() = FeedLike::class.java

    override val isForPremium: Boolean
        get() = true

    override val feedsType: FeedsCache.FEEDS_TYPE
        get() = FeedsCache.FEEDS_TYPE.DATA_LIKES_FEEDS

    override fun itemClick(view: View?, itemPosition: Int, data: FeedLike?, from: String) {
        super.itemClick(view, itemPosition, data, TYPE_FEED_FRAGMENT)
        ReadLikeRequest(context, data.getUserId()).exec()
    }
}