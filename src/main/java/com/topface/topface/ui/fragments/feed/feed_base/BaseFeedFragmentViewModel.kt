package com.topface.topface.ui.fragments.feed.feed_base

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.databinding.ObservableBoolean
import android.databinding.ObservableInt
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import android.support.v4.widget.SwipeRefreshLayout
import android.text.TextUtils
import android.view.View
import com.topface.framework.utils.Debug
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.data.CountersData
import com.topface.topface.data.FeedItem
import com.topface.topface.data.FeedListData
import com.topface.topface.data.FixedViewInfo
import com.topface.topface.databinding.FragmentFeedBaseBinding
import com.topface.topface.requests.FeedRequest
import com.topface.topface.requests.handlers.ErrorCodes
import com.topface.topface.state.TopfaceAppState
import com.topface.topface.ui.fragments.ChatFragment
import com.topface.topface.ui.fragments.feed.app_day.AppDay
import com.topface.topface.ui.fragments.feed.feed_api.FeedApi
import com.topface.topface.ui.fragments.feed.feed_utils.getFirst
import com.topface.topface.utils.RxUtils
import com.topface.topface.utils.Utils
import com.topface.topface.utils.config.FeedsCache
import com.topface.topface.utils.debug.FuckingVoodooMagic
import com.topface.topface.utils.extensions.safeUnsubscribe
import com.topface.topface.utils.gcmutils.GCMUtils
import com.topface.topface.viewModels.BaseViewModel
import rx.Observer
import rx.Subscriber
import rx.Subscription
import java.util.*
import javax.inject.Inject

/**
 * Базовая VM для всех фидов
 * Created by tiberal on 01.08.16.
 * @param T feed item type
 */
abstract class BaseFeedFragmentViewModel<T : FeedItem>(binding: FragmentFeedBaseBinding, private val mNavigator: IFeedNavigator,
                                                       private val mApi: FeedApi) :
        BaseViewModel<FragmentFeedBaseBinding>(binding), SwipeRefreshLayout.OnRefreshListener {

    @Inject lateinit var mState: TopfaceAppState
    var isRefreshing = object : ObservableBoolean() {
        override fun set(value: Boolean) {
            super.set(value)
            if (!value) {
                onRefreshed()
            }
        }
    }
    val isLockViewVisible = ObservableInt(View.INVISIBLE)
    val isFeedProgressBarVisible = ObservableInt(View.INVISIBLE)
    val isListVisible = ObservableInt(View.VISIBLE)
    var stubView: IFeedLockerView? = null
    private val mUnreadState = FeedRequest.UnreadStatePair(true, false)
    protected val mAdapter: BaseFeedAdapter<*, T>? by lazy {
        binding.feedList.adapter as? BaseFeedAdapter<*, T>
    }

    abstract val feedsType: FeedsCache.FEEDS_TYPE
    abstract val itemClass: Class<T>
    abstract val service: FeedRequest.FeedService
    abstract val gcmType: Array<Int>
    open val typeFeedFragment: String? = null
    abstract fun isCountersChanged(newCounters: CountersData, currentCounters: CountersData): Boolean
    open val gcmTypeUpdateAction: String? = null
    open val isForPremium: Boolean = false
    open val isNeedReadItems: Boolean = false
    open val isNeedCacheItems: Boolean = true
    open val bannerRes: Int = R.layout.app_day_list
    private val mCounters by lazy {
        CountersData()
    }
    private val mCache by lazy {
        FeedCacheManager<T>(context, feedsType)
    }
    private var mCallUpdateSubscription: Subscription? = null
    private var mUpdaterSubscription: Subscription? = null
    private var mAppDayRequestSubscription: Subscription? = null
    private var mDeleteSubscription: Subscription? = null
    private var mBlackListSubscription: Subscription? = null
    private var mCountersSubscription: Subscription? = null

    private var isDataFromCache: Boolean = false

    companion object {
        val FROM = "from"
        val TO = "to"
        val SERVICE = "service"
        val LEAVE = "leave"
        val HISTORY_LOAD_FLAG = "history_load_flag"
        val PULL_TO_REF_FLAG = "pull_to_refresh_flag"
        val UNREAD_STATE = "unread_state"
    }

    private lateinit var mReadItemReceiver: BroadcastReceiver
    private lateinit var mGcmReceiver: BroadcastReceiver

    init {
        App.get().inject(this)
        if (isNeedCacheItems) {
            mCache.restoreFromCache(itemClass)?.let {
                mAdapter?.let { adapter ->
                    if (adapter.data.isEmpty()) {
                        adapter.addData(it)
                        isDataFromCache = true
                    }
                }
            }
        } else {
            isListVisible.set(View.INVISIBLE)
        }
        mUpdaterSubscription = mAdapter?.let { adapter ->
            adapter.updaterObservable.distinct {
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
        }
        mCountersSubscription = mState.getObservable(CountersData::class.java)
                .filter { newCounters ->
                    val isChanged = mCounters.isNotEmpty && isCountersChanged(newCounters, mCounters)
                    mCounters.setCounters(newCounters)
                    isChanged
                }
                .subscribe(object : RxUtils.ShortSubscription<CountersData>() {
                    override fun onNext(newCounters: CountersData?) {
                        super.onNext(newCounters)
                        newCounters?.let {
                            loadTopFeeds()
                        }
                    }
                })
        createAndRegisterBroadcasts()
    }

    @FuckingVoodooMagic(description = "Эхо некрокода! Как только переделем остальные фрагмент на новый лад это нужно заменить на ивенты")
    open protected fun createAndRegisterBroadcasts() {
        mGcmReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                for (type in gcmType) {
                    GCMUtils.cancelNotification(context, type)
                }
                loadTopFeeds()
            }
        }
        mReadItemReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val itemId = intent.getStringExtra(ChatFragment.INTENT_ITEM_ID)
                val userId = intent.getIntExtra(ChatFragment.INTENT_USER_ID, 0)
                if (userId == 0) {
                    if (!TextUtils.isEmpty(itemId)) {
                        makeItemReadWithFeedId(itemId)
                    }
                } else {
                    makeItemReadUserId(userId, intent.getIntExtra(ChatFragment.LOADED_MESSAGES, 0))
                }
            }
        }
        val filter = IntentFilter(ChatFragment.MAKE_ITEM_READ)
        filter.addAction(ChatFragment.MAKE_ITEM_READ_BY_UID)
        LocalBroadcastManager.getInstance(context).registerReceiver(mReadItemReceiver, filter)
        gcmTypeUpdateAction?.let {
            LocalBroadcastManager.getInstance(context).registerReceiver(mGcmReceiver, IntentFilter(it))
        }
    }

    open fun makeItemReadWithFeedId(id: String) = mAdapter?.let { adapter ->
        adapter.data.forEachIndexed { position, dataItem ->
            if (TextUtils.equals(dataItem.id, id) && dataItem.unread) {
                dataItem.unread = false
            }
            adapter.notifyItemChanged(position)
        }
    }

    protected fun makeItemReadUserId(uid: Int, readMessages: Int) =
            mAdapter?.let { adapter ->
                adapter.data.forEachIndexed { position, dataItem ->
                    if (dataItem.user != null && dataItem.user.id == uid && dataItem.unread) {
                        val unread = dataItem.unreadCounter - readMessages
                        if (unread > 0) {
                            dataItem.unreadCounter = unread
                        } else {
                            dataItem.unread = false
                            dataItem.unreadCounter = 0
                        }
                        adapter.notifyItemChange(position)
                    }
                }
            }

    fun update(updateBundle: Bundle = Bundle()) {
        isFeedProgressBarVisible.set(View.VISIBLE)
        mCallUpdateSubscription = mApi.callFeedUpdate(isForPremium, itemClass,
                constructFeedRequestArgs(isPullToRef = false, to = updateBundle.getString(TO, Utils.EMPTY))).
                subscribe(object : Observer<FeedListData<T>> {
                    override fun onCompleted() {
                        mCallUpdateSubscription.safeUnsubscribe()
                        isFeedProgressBarVisible.set(View.INVISIBLE)
                    }

                    override fun onError(e: Throwable?) {
                        e?.let {
                            onErrorProcess(it)
                        }
                        mCallUpdateSubscription.safeUnsubscribe()
                        isFeedProgressBarVisible.set(View.INVISIBLE)
                    }

                    override fun onNext(data: FeedListData<T>?) = updateFeedsLoaded(data, updateBundle)
                })
    }

    protected open fun updateFeedsLoaded(data: FeedListData<T>?, updateBundle: Bundle) {
        data?.let {
            if (isDataFromCache) {
                mAdapter?.let { adapter ->
                    adapter.data.clear()
                    adapter.notifyDataSetChanged()
                    typeFeedFragment?.let { getAppDayRequest(it) }
                }
                isDataFromCache = false
            }
            handleUnreadState(it, updateBundle.getBoolean(PULL_TO_REF_FLAG))
            val adapter = mAdapter
            if (adapter != null && adapter.data.isEmpty() && data.items.isEmpty()) {
                isListVisible.set(View.INVISIBLE)
                stubView?.onEmptyFeed()
            } else {
                isListVisible.set(View.VISIBLE)
                isLockViewVisible.set(View.GONE)
                stubView?.onFilledFeed()
            }
            adapter?.addData(data.items)
        }
    }

    fun getAppDayRequest(typeFeedFragment: String) {
        mAppDayRequestSubscription = mApi.getAppDayRequest(typeFeedFragment).subscribe(object : Subscriber<AppDay>() {
            override fun onCompleted() {
            }

            override fun onError(e: Throwable?) =
                    e?.let { Debug.log("App day banner error request: $it") } ?: Unit

            override fun onNext(appDay: AppDay?) = appDay?.list?.let { imageArray ->
                if (!imageArray.isEmpty()) {
                    mAdapter?.setHeader(FixedViewInfo(bannerRes, imageArray))
                    mAdapter?.notifyItemChange(0)
                }

            } ?: Unit
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
                if (mAdapter?.data == null) {
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
        loadTopFeeds()
    }

    open fun onRefreshed() {
    }

    fun loadTopFeeds() {
        val from = mAdapter?.data?.getFirst()?.let {
            it.id
        } ?: Utils.EMPTY
        val requestBundle = constructFeedRequestArgs(from = from, to = null)
        mCallUpdateSubscription = mApi.callFeedUpdate(isForPremium, itemClass, requestBundle)
                .subscribe(object : Subscriber<FeedListData<T>>() {
                    override fun onCompleted() {
                        if (isRefreshing.get()) {
                            isRefreshing.set(false)
                        }
                    }

                    override fun onError(e: Throwable?) {
                        e?.let {
                            onErrorProcess(it)
                        }
                        if (isRefreshing.get()) {
                            isRefreshing.set(false)
                        }
                    }

                    override fun onNext(data: FeedListData<T>?) = topFeedsLoaded(data, requestBundle)

                })
    }

    protected open fun topFeedsLoaded(data: FeedListData<T>?, requestBundle: Bundle) {
        data?.let {
            if (!it.items.isEmpty()) {
                if (mAdapter?.itemCount == 0) {
                    isListVisible.set(View.VISIBLE)
                    isLockViewVisible.set(View.GONE)
                    stubView?.onFilledFeed()
                }
                handleUnreadState(it, requestBundle.getBoolean(PULL_TO_REF_FLAG))
                removeOldDuplicates(it)
                mAdapter?.addFirst(it.items)
                binding.feedList.layoutManager.scrollToPosition(0)
            }
        }
    }

    @FuckingVoodooMagic(description = "Если нет новых фидов сервер присылает итем от которого проходила выборка(первый)")
    protected fun removeOldDuplicates(data: FeedListData<T>) {
        mAdapter?.let { adapter ->
            val feedsIterator = adapter.data.iterator()
            while (feedsIterator.hasNext()) {
                val feed = feedsIterator.next()
                for (newFeed in data.items) {
                    if (considerDuplicates(feed, newFeed)) {
                        val pos = adapter.data.indexOf(feed)
                        feedsIterator.remove()
                        adapter.notifyItemRemove(pos)
                        break
                    }
                }
            }
        }
    }

    @FuckingVoodooMagic(description = "нужен для removeOldDuplicates")
    protected open fun considerDuplicates(first: T, second: T) = if (first.id == null) second.id == null else first.id == second.id

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


    private fun hasData(): Boolean {
        val adapter = mAdapter
        return adapter != null && !adapter.data.isEmpty()
    }

    fun onDeleteFeedItems(items: MutableList<T>, idsList: ArrayList<String>) {
        val tempItems = items.toList()
        isLockViewVisible.set(View.VISIBLE)
        mDeleteSubscription = mApi.callDelete(feedsType, idsList).subscribe(createSubscriber(tempItems))
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
            isLockViewVisible.set(View.GONE)
        }

        override fun onNext(appDay: Boolean?) {
            mAdapter?.let { adapter ->
                adapter.removeItems(items)
                if (adapter.data.isEmpty()) {
                    isListVisible.set(View.INVISIBLE)
                    stubView?.onEmptyFeed()
                }
                mCache.saveToCache(adapter.data)
            }
        }
    }

    override fun release() {
        super.release()
        RxUtils.safeUnsubscribe(mUpdaterSubscription)
        RxUtils.safeUnsubscribe(mCallUpdateSubscription)
        RxUtils.safeUnsubscribe(mDeleteSubscription)
        RxUtils.safeUnsubscribe(mBlackListSubscription)
        RxUtils.safeUnsubscribe(mCountersSubscription)
        RxUtils.safeUnsubscribe(mAppDayRequestSubscription)
        if (isNeedCacheItems) {
            mAdapter?.let { adapter ->
                if (!adapter.data.isEmpty()) {
                    mCache.saveToCache(adapter.data)
                }
            }
        }
        LocalBroadcastManager.getInstance(context).unregisterReceiver(mReadItemReceiver)
        LocalBroadcastManager.getInstance(context).unregisterReceiver(mGcmReceiver)
    }
}
