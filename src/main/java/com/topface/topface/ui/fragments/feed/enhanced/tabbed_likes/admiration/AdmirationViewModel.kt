package com.topface.topface.ui.fragments.feed.enhanced.tabbed_likes.admiration

import android.view.View
import com.topface.topface.App
import com.topface.topface.api.FeedRequestFactory
import com.topface.topface.api.IApi
import com.topface.topface.api.responses.FeedBookmark
import com.topface.topface.api.responses.GetFeedBookmarkListResponse
import com.topface.topface.api.responses.IBaseFeedResponse
import com.topface.topface.data.CountersData
import com.topface.topface.ui.fragments.feed.dialogs.PopupMenuFragment
import com.topface.topface.ui.fragments.feed.enhanced.base.BaseFeedFragmentModel
import com.topface.topface.ui.fragments.feed.enhanced.tabbed_likes.PopupMenuAddToBlackListEvent
import com.topface.topface.ui.fragments.feed.enhanced.tabbed_likes.PopupMenuDeleteEvent
import com.topface.topface.ui.fragments.feed.feed_utils.getUserId
import com.topface.topface.utils.config.FeedsCache
import com.topface.topface.utils.gcmutils.GCMUtils
import com.topface.topface.utils.rx.safeUnsubscribe
import rx.Observable
import rx.Subscription

class AdmirationViewModel(api: IApi) : BaseFeedFragmentModel<FeedBookmark>(api) {

    private val mEventBus by lazy {
        App.getAppComponent().eventBus()
    }

    private var mPopupMenuSubscription: Subscription? = null

    init {
        mPopupMenuSubscription = Observable.merge(
                mEventBus.getObservable(PopupMenuDeleteEvent::class.java),
                mEventBus.getObservable(PopupMenuAddToBlackListEvent::class.java)
        )
                .filter { it.getPopupType() == PopupMenuFragment.ADMIRATION_TYPE }
                .subscribe { remove(it.getItemForAction().getUserId()) }
    }

    override val responseClass: Class<out IBaseFeedResponse>
        get() = GetFeedBookmarkListResponse::class.java
    override val feedsType: FeedsCache.FEEDS_TYPE
        get() = FeedsCache.FEEDS_TYPE.DATA_ADMIRATION_FEEDS
    override val itemClass: Class<FeedBookmark>
        get() = FeedBookmark::class.java
    override val service: FeedRequestFactory.FeedService
        get() = FeedRequestFactory.FeedService.ADMIRATIONS
    override val gcmType: Array<Int>
        get() = arrayOf(GCMUtils.GCM_TYPE_UNKNOWN)
    override val isForPremium: Boolean
        get() = true

    override fun isCountersChanged(newCounters: CountersData, currentCounters: CountersData) =
            newCounters.admirations > currentCounters.admirations

    override fun considerDuplicates(first: FeedBookmark, second: FeedBookmark) =
            first.user?.id == second.user?.id

    override fun itemClick(view: View?, itemPosition: Int, data: FeedBookmark?, from: String) =
            navigator?.showProfile(data, from)

    override fun release() {
        super.release()
        mPopupMenuSubscription.safeUnsubscribe()
    }
}