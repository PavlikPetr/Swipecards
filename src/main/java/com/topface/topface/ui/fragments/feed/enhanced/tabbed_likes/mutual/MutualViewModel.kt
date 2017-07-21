package com.topface.topface.ui.fragments.feed.enhanced.tabbed_likes.mutual

import android.view.View
import com.topface.framework.utils.Debug
import com.topface.topface.api.FeedRequestFactory
import com.topface.topface.api.IApi
import com.topface.topface.api.responses.FeedBookmark
import com.topface.topface.api.responses.GetFeedBookmarkListResponse
import com.topface.topface.api.responses.IBaseFeedResponse
import com.topface.topface.data.CountersData
import com.topface.topface.ui.fragments.feed.enhanced.base.BaseFeedFragmentModel
import com.topface.topface.utils.config.FeedsCache
import com.topface.topface.utils.gcmutils.GCMUtils
import com.topface.topface.utils.rx.shortSubscribe
import rx.subscriptions.CompositeSubscription

class MutualViewModel(val api: IApi) : BaseFeedFragmentModel<FeedBookmark>(api) {
    override val responseClass: Class<out IBaseFeedResponse>
        get() = GetFeedBookmarkListResponse::class.java
    override val feedsType: FeedsCache.FEEDS_TYPE
        get() = FeedsCache.FEEDS_TYPE.DATA_MUTUALS_FEEDS
    override val itemClass: Class<FeedBookmark>
        get() = FeedBookmark::class.java
    override val service: FeedRequestFactory.FeedService
        get() = FeedRequestFactory.FeedService.MUTUAL
    override val gcmType: Array<Int>
        get() = arrayOf(GCMUtils.GCM_TYPE_UNKNOWN)
    override val isForPremium: Boolean
        get() = true

    private val mPopupMenuSubscription = CompositeSubscription()

    override fun isCountersChanged(newCounters: CountersData, currentCounters: CountersData) =
            newCounters.mutual > currentCounters.mutual

    override fun considerDuplicates(first: FeedBookmark, second: FeedBookmark) =
            first.user?.id == second.user?.id

    override fun itemClick(view: View?, itemPosition: Int, data: FeedBookmark?, from: String) =
            navigator?.showProfile(data, from)

    init {
        mPopupMenuSubscription
                .add(api.observeDeleteMutual().shortSubscribe {
                    //todo       обработать получение ивента
                })
        mPopupMenuSubscription.add(api.observeAddToBlackList().shortSubscribe {
            //todo       обработать получение ивента
        })
    }

    // TODO                    ОТПИСКУ НЕ ПРОЕБИ!!!!!!!!!!!!!!
}