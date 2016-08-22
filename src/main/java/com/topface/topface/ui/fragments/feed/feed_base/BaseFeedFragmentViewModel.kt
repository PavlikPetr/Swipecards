package com.topface.topface.ui.fragments.feed.feed_base

import android.databinding.ObservableBoolean
import android.databinding.ObservableInt
import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.view.View
import com.topface.topface.data.FeedItem
import com.topface.topface.data.FeedListData
import com.topface.topface.databinding.FragmentFeedBaseBinding
import com.topface.topface.requests.FeedRequest
import com.topface.topface.requests.handlers.ErrorCodes
import com.topface.topface.ui.fragments.feed.feed_api.FeedApi
import com.topface.topface.ui.fragments.feed.feed_utils.getFirst
import com.topface.topface.utils.RxUtils
import com.topface.topface.utils.Utils
import com.topface.topface.utils.config.FeedsCache
import com.topface.topface.utils.debug.FuckingVoodooMagic
import com.topface.topface.viewModels.BaseViewModel
import rx.Observer
import rx.Subscriber
import rx.Subscription

/**
 * Базовая VM для всех фидов
 * Created by tiberal on 01.08.16.
 */
abstract class BaseFeedFragmentViewModel<T : FeedItem>(binding: FragmentFeedBaseBinding, private val mNavigator: IFeedNavigator,
                                                       private val mApi: FeedApi) : BaseViewModel<FragmentFeedBaseBinding>(binding), SwipeRefreshLayout.OnRefreshListener {

    val isRefreshing = ObservableBoolean()
    val isLockViewVisible = ObservableInt(View.INVISIBLE)
    val isFeedProgressBarVisible = ObservableInt(View.INVISIBLE)
    val isListVisible = ObservableInt(View.VISIBLE)
    var stubView: IFeedLockerView? = null
    private val mUnreadState = FeedRequest.UnreadStatePair(true, false)
    private val mAdapter by lazy {
        binding.feedList.adapter as BaseFeedAdapter<T>
    }
    abstract val feedsType: FeedsCache.FEEDS_TYPE
    abstract val isForPremium: Boolean
    abstract val itemClass: Class<T>
    abstract val service: FeedRequest.FeedService
    open val isNeedReadItems: Boolean = false
    private val mCache by lazy {
        FeedCacheManager<T>(context, feedsType)
    }
    private var mCallUpdateSbscription: Subscription? = null
    private var mUpdaterSubscription: Subscription? = null
    private var mDeleteSubscription: Subscription? = null
    private var mBlackListSubscription: Subscription? = null

    companion object {
        val FROM = "from"
        val TO = "to"
        val SERVICE = "service"
        val LEAVE = "leave"
        val HISTORY_LOAD_FLAG = "history_load_flag"
        val PULL_TO_REF_FLAG = "pull_to_refresh_flag"
        val UNREAD_STATE = "unread_state"
    }

    init {
        /*mCache.restoreFromCache(itemClass)?.let {
            if (mAdapter.data.isEmpty()) {
                mAdapter.addData(it)
            }
        }*/
        mUpdaterSubscription = mAdapter.updaterObservable.distinct {
            it?.let {
                it.getString(TO, Utils.EMPTY)
            }
        }.subscribe(object : RxUtils.ShortSubscription<Bundle>() {
            override fun onNext(updateBundle: Bundle?) {
                updateBundle?.let {
                    update(it)
                }
            }
        })
        createAndRegisterBroadcasts()
    }

    @FuckingVoodooMagic(description = "Эхо некрокода! Как только переделем остальные фрагмент на новый лад это нужно заменить на ивенты")
    private fun createAndRegisterBroadcasts(){

    }

    fun update(updateBundle: Bundle = Bundle()) {
        isFeedProgressBarVisible.set(View.VISIBLE)
        mCallUpdateSbscription = mApi.callUpdate(isForPremium, itemClass,
                constructFeedRequestArgs(isPullToRef = false, to = updateBundle.getString(TO, Utils.EMPTY))).
                subscribe(object : Observer<FeedListData<T>> {
                    override fun onCompleted() {
                        RxUtils.safeUnsubscribe(mCallUpdateSbscription)
                        isFeedProgressBarVisible.set(View.INVISIBLE)
                    }

                    override fun onError(e: Throwable?) {
                        e?.let {
                            onErrorProcess(it)
                        }
                    }

                    override fun onNext(data: FeedListData<T>?) {
                        data?.let {
                            handleUnreadState(it, updateBundle.getBoolean(PULL_TO_REF_FLAG))
                            if (mAdapter.data.isEmpty() && data.items.isEmpty()) {
                                stubView?.onEmptyFeed()
                            } else {
                                isListVisible.set(View.VISIBLE)
                            }
                            mAdapter.addData(data.items)
                        }
                    }
                })
    }

    private fun onErrorProcess(e: Throwable) {
        e.printStackTrace()
        val codeError = Integer.valueOf(e.message)
        when (codeError) {
            ErrorCodes.PREMIUM_ACCESS_ONLY, ErrorCodes.BLOCKED_SYMPATHIES
                , ErrorCodes.BLOCKED_PEOPLE_NEARBY -> {
                isListVisible.set(View.INVISIBLE)
                isFeedProgressBarVisible.set(View.INVISIBLE)
                stubView?.onLockedFeed(codeError)
                return
            }
            else -> {
                if (mAdapter.data == null) {
                    isFeedProgressBarVisible.set(View.VISIBLE)
                }
                stubView?.onFilledFeed()
            }
        }
    }

    private fun handleUnreadState(data: FeedListData<T>, isPullToRef: Boolean) {
        if (!data.items.isEmpty()) {
            if (!mUnreadState.wasFromInited || isPullToRef) {
                mUnreadState.from = data.items.first.unread
                mUnreadState.wasFromInited = true
            }
            mUnreadState.to = data.items.last.unread
        }
    }

    open fun itemClick(view: View?, itemPosition: Int, data: T?) = mNavigator.showChat(data)

    override fun onRefresh() {
        isRefreshing.set(true)
        val from = mAdapter.data.getFirst()?.let {
            it.id
        } ?: Utils.EMPTY
        val requestBundle = constructFeedRequestArgs(from = from, to = null)
        mCallUpdateSbscription = mApi.callUpdate(isForPremium, itemClass, requestBundle)
                .subscribe(object : Subscriber<FeedListData<T>>() {
                    override fun onCompleted() {
                        isRefreshing.set(false)
                    }

                    override fun onError(e: Throwable?) {
                        e?.let {
                            onErrorProcess(it)
                        }
                    }

                    override fun onNext(data: FeedListData<T>?) {
                        data?.let {
                            if (!data.items.isEmpty()) {
                                handleUnreadState(it, requestBundle.getBoolean(PULL_TO_REF_FLAG))
                                removeOldDuplicates(data)
                                mAdapter.addData(data.items, 0)
                            }
                        }
                    }
                })
    }

    @FuckingVoodooMagic(description = "Если нет новых фидов сервер присылает итем от которого проходила выборка(первый)")
    protected fun removeOldDuplicates(data: FeedListData<T>) {
        val feedsIterator = mAdapter.data.iterator()
        while (feedsIterator.hasNext()) {
            val feed = feedsIterator.next()
            for (newFeed in data.items) {
                if (considerDuplicates(feed, newFeed)) {
                    feedsIterator.remove()
                    break
                }
            }
        }
    }

    @FuckingVoodooMagic(description = "нужен для removeOldDuplicates")
    protected fun considerDuplicates(first: T, second: T): Boolean {
        return if (first.id == null) second.id == null else first.id == second.id
    }

    private fun constructFeedRequestArgs(isPullToRef: Boolean = true, from: String? = Utils.EMPTY,
                                         to: String? = Utils.EMPTY) =
            Bundle().apply {
                putSerializable(SERVICE, service)
                putParcelable(UNREAD_STATE, mUnreadState)
                putBoolean(PULL_TO_REF_FLAG, isPullToRef)
                putString(FROM, from)
                putString(TO, to)
                putBoolean(HISTORY_LOAD_FLAG, hasData())
                putBoolean(LEAVE, isNeedReadItems)
            }


    private fun hasData() = !mAdapter.data.isEmpty()

    fun onDeleteFeedItems(items: MutableList<T>) {
        val tempItems = items.toList()
        isLockViewVisible.set(View.VISIBLE)
        mDeleteSubscription = mApi.callDelete(feedsType, tempItems).subscribe(createSubscriber(tempItems))
    }

    fun onAddToBlackList(items: MutableList<T>) {
        val tempItems = items.toList()
        isLockViewVisible.set(View.VISIBLE)
        mBlackListSubscription = mApi.callAddToBlackList(items).subscribe(createSubscriber(tempItems))
    }

    private fun createSubscriber(items: List<T>) = object : Subscriber<Boolean>() {
        override fun onCompleted() {
            isLockViewVisible.set(View.GONE)
        }

        override fun onError(e: Throwable?) {
            Utils.showErrorMessage()
        }

        override fun onNext(t: Boolean?) {
            mAdapter.removeItems(items)
            if (mAdapter.data.isEmpty()) {
                isListVisible.set(View.INVISIBLE)
                stubView?.onEmptyFeed()
            }
        }
    }


    override fun release() {
        super.release()
        RxUtils.safeUnsubscribe(mUpdaterSubscription)
        RxUtils.safeUnsubscribe(mCallUpdateSbscription)
        RxUtils.safeUnsubscribe(mDeleteSubscription)
        RxUtils.safeUnsubscribe(mBlackListSubscription)
        if (!mAdapter.data.isEmpty()) {
            //   mCache.saveToCache(mAdapter.data)
        }
    }
}
