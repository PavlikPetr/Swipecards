package com.topface.topface.ui.fragments.feed.enhanced.tabbed_likes

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
import com.topface.topface.ui.fragments.feed.enhanced.base.BaseFeedLockerController
import com.topface.topface.ui.fragments.feed.enhanced.base.LockerStubLastState
import com.topface.topface.ui.fragments.feed.feed_utils.getUserId
import com.topface.topface.utils.config.FeedsCache
import com.topface.topface.utils.gcmutils.GCMUtils
import com.topface.topface.utils.rx.shortSubscription
import rx.subscriptions.CompositeSubscription

/**
 * Базовая вьюмодель для табов: взаимные и возхищения
 */
abstract class BaseSympathyFeedFragmentViewModel(api: IApi) : BaseFeedFragmentModel<FeedBookmark>(api) {

    private val mEventBus by lazy {
        App.getAppComponent().eventBus()
    }

    private var mPopupMenuSubscription = CompositeSubscription()

    init {
        mPopupMenuSubscription.add(mEventBus.getObservable(PopupMenuDeleteEvent::class.java)
                .filter { it.getPopupType() == sympathyTypeViewModelType }
                .subscribe(shortSubscription {
                    val itemIdForDelete = it.getItemForAction()
                    val userId = itemIdForDelete.getUserId()
                    when (sympathyTypeViewModelType) {
                        PopupMenuFragment.ADMIRATION_TYPE -> {
                            api.callDeleteAdmiration(arrayListOf(itemIdForDelete.id)).subscribe { remove(userId) }
                        }
                        PopupMenuFragment.MUTUAL_TYPE -> {
                            api.callDeleteMutual(arrayListOf(itemIdForDelete.id)).subscribe { remove(userId) }
                        }
                    }

                }
                ))
        mPopupMenuSubscription.add(mEventBus.getObservable(PopupMenuAddToBlackListEvent::class.java)
                .filter { it.getPopupType() == sympathyTypeViewModelType }
                .subscribe(shortSubscription {
                    val itemForDelete = it.getItemForAction()
                    api.callAddToBlackList(arrayListOf(itemForDelete)).subscribe { remove(itemForDelete.getUserId()) }
                }
                ))
    }

    abstract val sympathyTypeViewModelType: Long

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

}