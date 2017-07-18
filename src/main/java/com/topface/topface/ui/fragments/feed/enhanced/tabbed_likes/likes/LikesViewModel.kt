package com.topface.topface.ui.fragments.feed.enhanced.tabbed_likes.likes

import android.databinding.ObservableField
import android.databinding.ObservableFloat
import android.databinding.ObservableInt
import android.os.Bundle
import android.view.View
import com.lorentzos.flingswipe.SwipeFlingAdapterView
import com.topface.framework.utils.Debug
import com.topface.scruffy.data.ApiError
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.api.FeedRequestFactory
import com.topface.topface.api.UnreadStatePair
import com.topface.topface.api.responses.FeedBookmark
import com.topface.topface.api.responses.GetFeedBookmarkListResponse
import com.topface.topface.api.responses.IBaseFeedResponse
import com.topface.topface.data.CountersData
import com.topface.topface.data.FeedItem
import com.topface.topface.data.FeedLike
import com.topface.topface.requests.handlers.ErrorCodes
import com.topface.topface.ui.fragments.feed.enhanced.base.BaseFeedFragmentModel
import com.topface.topface.ui.fragments.feed.enhanced.base.BaseFeedLockerController
import com.topface.topface.ui.fragments.feed.enhanced.base.BaseViewModel
import com.topface.topface.ui.fragments.feed.enhanced.base.LockerStubState
import com.topface.topface.ui.fragments.feed.feed_base.FeedCacheManager
import com.topface.topface.ui.fragments.feed.feed_base.IFeedLockerView
import com.topface.topface.utils.Utils
import com.topface.topface.utils.config.FeedsCache
import com.topface.topface.utils.databinding.MultiObservableArrayList
import com.topface.topface.utils.extensions.unregisterReceiver
import com.topface.topface.utils.rx.safeUnsubscribe
import com.topface.topface.utils.rx.shortSubscribe
import rx.Observable
import rx.Subscriber
import rx.Subscription
import java.util.ArrayList
import java.util.concurrent.atomic.AtomicReference

/**
 * Created by ppavlik on 17.07.17.
 * Вью-модель симпатий в виде карточек, по аналогии с tinder
 */
typealias LockerStubLastState = Pair<Long, Int>
class LikesViewModel : BaseViewModel(), SwipeFlingAdapterView.onFlingListener, SwipeFlingAdapterView.OnItemClickListener {

    val data = MultiObservableArrayList<Any>()

    val isFeedProgressBarVisible = ObservableInt(View.INVISIBLE)
    val isListVisible = ObservableInt(View.VISIBLE)
    val isLockViewVisible = ObservableInt(View.INVISIBLE)
    val scrollProgressPercent = ObservableFloat(0f)

    private val mUnreadState = UnreadStatePair(true, false)
    private var mUpdaterSubscription: Subscription? = null
    private var mIsAllDataLoaded = false
    private val mAtomicUpdaterSubscription = AtomicReference<Subscription>()
    open val isNeedCacheItems: Boolean = false
    private var isDataFromCache = false
    open val typeFeedFragment: String? = null
    var stubView: IFeedLockerView? = null
        set(value) {
            field = value
            switchLockerStubView(lockerStubLastState.first, field, lockerStubLastState.second)
        }

    private var lockerStubLastState = LockerStubLastState(BaseFeedLockerController.NONE, -666)

    var updateObservable: Observable<Bundle>? = null
        set(value) {
            mUpdaterSubscription = value?.let {
                it.distinct {
                    it?.getString(TO, Utils.EMPTY)
                }.shortSubscribe { updateBundle ->
                    if (!mIsAllDataLoaded) {
                        updateBundle?.let {
                            update(it)
                        }
                    }
                }
            }
            field = value
        }

    private val mCache by lazy {
        FeedCacheManager<FeedBookmark>(FeedsCache.FEEDS_TYPE.DATA_LIKES_FEEDS)
    }

    companion object {
        const val FROM = "from"
        const val TO = "to"
        const val SERVICE = "service"
        const val LEAVE = "leave"
        const val HISTORY_LOAD_FLAG = "history_load_flag"
        const val PULL_TO_REF_FLAG = "pull_to_refresh_flag"
        const val UNREAD_STATE = "unread_state"
    }

    private val mApi by lazy {
        App.getAppComponent().api()
    }

    fun update(updateBundle: Bundle = Bundle(), force: Boolean = false) {
        if (mAtomicUpdaterSubscription.get() == null && (!isNeedCacheItems || isDataFromCache) ||
                mAtomicUpdaterSubscription.get()?.isUnsubscribed ?: false &&
                        updateBundle.getString(BaseFeedFragmentModel.TO, Utils.EMPTY) != Utils.EMPTY || force) {
            isFeedProgressBarVisible.set(View.VISIBLE)
            val arg = constructFeedRequestArgs(isPullToRef = false, to = updateBundle.getString(BaseFeedFragmentModel.TO, Utils.EMPTY))
            mAtomicUpdaterSubscription.set(mApi.callGetList(arg, GetFeedBookmarkListResponse::class.java, FeedBookmark::class.java).
                    subscribe(object : Subscriber<IBaseFeedResponse>() {
                        override fun onNext(data: IBaseFeedResponse?) {
                            data?.let {
                                if (!data.more) mIsAllDataLoaded = true
                                updateFeedsLoaded(data.getItemsList(), updateBundle)
                            }
                        }

                        override fun onError(e: Throwable?) {
                            mAtomicUpdaterSubscription.get().safeUnsubscribe()
                            onErrorProcess(e as? ApiError)
                            isFeedProgressBarVisible.set(View.INVISIBLE)
                        }

                        override fun onCompleted() {
                            mAtomicUpdaterSubscription.get().safeUnsubscribe()
                            isFeedProgressBarVisible.set(View.INVISIBLE)
                        }
                    }))
        }
    }

    private fun onErrorProcess(apiError: ApiError?) = apiError?.let {
        it.printStackTrace()
        val codeError = Integer.valueOf(it.code)
        when (codeError) {
            ErrorCodes.PREMIUM_ACCESS_ONLY, ErrorCodes.BLOCKED_SYMPATHIES
                , ErrorCodes.BLOCKED_PEOPLE_NEARBY -> {
                isListVisible.set(View.INVISIBLE)
                isFeedProgressBarVisible.set(View.INVISIBLE)
                switchLockerStubView(BaseFeedLockerController.LOCKED_FEED, stubView, codeError)
                return@let
            }
            else -> {
                if (data.isEmpty()) {
                    isFeedProgressBarVisible.set(View.VISIBLE)
                }
                switchLockerStubView(BaseFeedLockerController.FILLED_FEED, stubView)
            }
        }
    }

    private fun switchLockerStubView(@LockerStubState state: Long, stubView: IFeedLockerView?,
                                     errorCode: Int = -666) = stubView?.let {
        lockerStubLastState = LockerStubLastState(state, errorCode)
        when (state) {
            BaseFeedLockerController.FILLED_FEED -> it.onFilledFeed()
            BaseFeedLockerController.EMPTY_FEED -> it.onEmptyFeed()
            BaseFeedLockerController.LOCKED_FEED -> it.onLockedFeed(errorCode)
            else -> {
                lockerStubLastState = LockerStubLastState(BaseFeedLockerController.NONE, errorCode)
            }
        }
    }

    private fun constructFeedRequestArgs(isPullToRef: Boolean = true, from: String? = Utils.EMPTY,
                                         to: String? = Utils.EMPTY) =
            Bundle().apply {
                putSerializable(BaseFeedFragmentModel.SERVICE, FeedRequestFactory.FeedService.LIKES)
                putParcelable(BaseFeedFragmentModel.UNREAD_STATE, mUnreadState)
                putBoolean(BaseFeedFragmentModel.PULL_TO_REF_FLAG, isPullToRef)
                putString(BaseFeedFragmentModel.FROM, from)
                putString(BaseFeedFragmentModel.TO, to)
                putBoolean(BaseFeedFragmentModel.HISTORY_LOAD_FLAG, hasData())
                putBoolean(BaseFeedFragmentModel.LEAVE, false)
            }

    private fun hasData() = data.isNotEmpty()

    private fun handleUnreadState(data: ArrayList<out FeedItem>, isPullToRef: Boolean) {
        if (!data.isEmpty()) {
            if (!mUnreadState.wasFromInited || isPullToRef) {
                mUnreadState.from = data.first().unread
                mUnreadState.wasFromInited = true
            }
            mUnreadState.to = data.last().unread
        }
    }

    protected open fun updateFeedsLoaded(newData: ArrayList<out FeedItem>, updateBundle: Bundle) {
        if (isDataFromCache) {
            data.clear()
            isDataFromCache = false
        }
        handleUnreadState(newData, updateBundle.getBoolean(BaseFeedFragmentModel.PULL_TO_REF_FLAG))
        if (data.isEmpty() && newData.isEmpty()) {
            isListVisible.set(View.INVISIBLE)
            switchLockerStubView(BaseFeedLockerController.EMPTY_FEED, stubView)
        } else {
            isListVisible.set(View.VISIBLE)
            isLockViewVisible.set(View.GONE)
            switchLockerStubView(BaseFeedLockerController.FILLED_FEED, stubView)
        }
        (newData as? ArrayList<FeedBookmark>)?.let {
            data.addAll(it)
        }
    }

    override fun onItemClicked(p0: Int, p1: Any?) {
    }

    override fun removeFirstObjectInAdapter() {
    }

    override fun onLeftCardExit(p0: Any?) {
        data.removeAt(0)
    }

    override fun onRightCardExit(p0: Any?) {
        data.removeAt(0)
    }

    override fun onAdapterAboutToEmpty(p0: Int) {
    }

    override fun onScroll(scrollProgressPercent: Float) {
        this@LikesViewModel.scrollProgressPercent.set(scrollProgressPercent)
    }

    override fun unbind() {
        mAtomicUpdaterSubscription.get().safeUnsubscribe()
        updateObservable = null
    }

    override fun release() {
        unbind()
        arrayOf(mUpdaterSubscription).safeUnsubscribe()
        if (isNeedCacheItems) {
            if (data.isNotEmpty()) {
                mCache.saveToCache(ArrayList<FeedBookmark>(data as List<FeedBookmark>))
            }
        }
    }

    init {
        update()
//        Debug.log("NEW_BASE new view model -> ${this}")
//        if (isNeedCacheItems) {
//            mCache.restoreFromCache(FeedBookmark::class.java)?.let {
//                if (data.isEmpty()) {
//                    data.addAll(it)
//                }
//            }
//            isDataFromCache = true
//        } else {
//            isListVisible.set(View.INVISIBLE)
//        }
//        mCountersSubscription = mState.getObservable(CountersData::class.java)
//                .filter { newCounters ->
//                    val isChanged = mCounters.isNotEmpty && isCountersChanged(newCounters, mCounters)
//                    mCounters.setCounters(newCounters)
//                    isChanged
//                }
//                .shortSubscribe { newCounters ->
//                    newCounters?.let {
//                        loadTopFeeds()
//                    }
//                }
//
//        createAndRegisterBroadcasts()
//        mStateManager.registerAppChangeStateListener(this)
    }

}
