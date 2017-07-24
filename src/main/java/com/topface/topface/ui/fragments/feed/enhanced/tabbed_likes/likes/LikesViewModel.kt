package com.topface.topface.ui.fragments.feed.enhanced.tabbed_likes.likes

import android.databinding.ObservableField
import android.databinding.ObservableFloat
import android.os.Bundle
import android.view.View
import com.lorentzos.flingswipe.SwipeFlingAdapterView
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.api.FeedRequestFactory
import com.topface.topface.api.IApi
import com.topface.topface.api.requests.LikeSendRequest
import com.topface.topface.api.responses.FeedBookmark
import com.topface.topface.api.responses.GetFeedBookmarkListResponse
import com.topface.topface.api.responses.IBaseFeedResponse
import com.topface.topface.data.CountersData
import com.topface.topface.data.FeedItem
import com.topface.topface.data.FeedUser
import com.topface.topface.ui.fragments.dating.mutual_popup.PopupMutualViewModel
import com.topface.topface.ui.fragments.feed.enhanced.base.BaseFeedFragmentModel
import com.topface.topface.utils.Utils
import com.topface.topface.utils.config.FeedsCache
import com.topface.topface.utils.gcmutils.GCMUtils
import com.topface.topface.utils.rx.safeUnsubscribe
import com.topface.topface.utils.rx.shortSubscribe
import rx.Observable
import rx.Subscriber
import rx.subscriptions.CompositeSubscription

class LikesViewModel(private val mApi: IApi) : BaseFeedFragmentModel<FeedBookmark>(mApi), SwipeFlingAdapterView.onFlingListener, SwipeFlingAdapterView.OnItemClickListener {
    override val responseClass: Class<out IBaseFeedResponse>
        get() = GetFeedBookmarkListResponse::class.java
    override val feedsType: FeedsCache.FEEDS_TYPE
        get() = FeedsCache.FEEDS_TYPE.DATA_LIKES_FEEDS
    override val itemClass: Class<FeedBookmark>
        get() = FeedBookmark::class.java
    override val service: FeedRequestFactory.FeedService
        get() = FeedRequestFactory.FeedService.LIKES
    override val gcmType: Array<Int>
        get() = arrayOf(GCMUtils.GCM_TYPE_UNKNOWN)
    override val isForPremium: Boolean
        get() = true
    override val isUnreadOnly: Boolean
        get() = true

    val counter = ObservableField(Utils.EMPTY)
    val scrollProgressPercent = ObservableFloat(0f)

    private var mUpdateSubscriber: Subscriber<in Bundle>? = null

    private val weakStorage by lazy {
        App.getAppComponent().weakStorage()
    }

    private val mSendLikeSubscriptions = CompositeSubscription()

    override fun isCountersChanged(newCounters: CountersData, currentCounters: CountersData): Boolean {
        counter.set(getCounter(newCounters.likes))
        return newCounters.likes > currentCounters.likes
    }

    override fun considerDuplicates(first: FeedBookmark, second: FeedBookmark) =
            first.user?.id == second.user?.id

    override fun itemClick(view: View?, itemPosition: Int, data: FeedBookmark?, from: String) =
            navigator?.showProfile(data, from)

    private fun getCounter(size: Int) = if (size == 0) {
        Utils.EMPTY
    } else {
        Utils.getQuantityString(R.plurals.number_of_sympathies, size, size)
    }

    override fun onItemClicked(itemPosition: Int, dataObject: Any?) {
        (dataObject as? FeedBookmark)?.let {
            navigator?.showProfile(it, LikesFragment.SCREEN_TYPE)
        }
    }

    override fun removeFirstObjectInAdapter() {
    }

    override fun onLeftCardExit(dataObject: Any?) {
        data.removeAt(0)
        (dataObject as? FeedBookmark)?.let {
            it.user?.id?.let {
                mApi.callReadLikeRequest(it)
            }
        }
    }

    override fun onRightCardExit(dataObject: Any?) {
        data.removeAt(0)
        (dataObject as? FeedBookmark)?.let {
            it.user?.id?.let {
                mSendLikeSubscriptions.add(mApi.callSendLikeRequest(it, LikeSendRequest.FROM_FEED).shortSubscribe {
                    if (!weakStorage.isSympathySended) {
                        weakStorage.setSympathySended()
                        weakStorage.saveConfig()
                        navigator?.showMutualPopup(FeedUser.createFeedUserFromUser(it.user), PopupMutualViewModel.SYMPATHY_TYPE)
                    }
                })

            }
        }
    }

    override fun onAdapterAboutToEmpty(itemsInAdapter: Int) {
        mUpdateSubscriber?.onNext(Bundle().apply {
            if (data.isNotEmpty()) {
                putString(FeedRequestFactory.TO, (data.last() as FeedItem).id)
            }
        })
    }

    override fun onScroll(scrollProgressPercent: Float) {
        this@LikesViewModel.scrollProgressPercent.set(scrollProgressPercent)
    }

    init {
        updateObservable = Observable.create { subscriber ->
            mUpdateSubscriber = subscriber
        }
    }

    override fun release() {
        super.release()
        mUpdateSubscriber = null
        mSendLikeSubscriptions.safeUnsubscribe()
    }
}