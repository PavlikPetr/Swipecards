package com.topface.topface.ui.fragments.feed.enhanced.base

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.databinding.ObservableBoolean
import android.databinding.ObservableInt
import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.text.TextUtils
import android.view.View
import com.topface.framework.utils.Debug
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.data.CountersData
import com.topface.topface.data.FeedItem
import com.topface.topface.data.FeedListData
import com.topface.topface.requests.FeedRequest
import com.topface.topface.requests.handlers.ErrorCodes
import com.topface.topface.ui.fragments.ChatFragment
import com.topface.topface.ui.fragments.feed.app_day.AppDay
import com.topface.topface.ui.fragments.feed.dialogs.dialogs_redesign.AppDayStubItem
import com.topface.topface.ui.fragments.feed.enhanced.utils.ImprovedObservableList
import com.topface.topface.ui.fragments.feed.feed_api.FeedApi
import com.topface.topface.ui.fragments.feed.feed_base.FeedCacheManager
import com.topface.topface.ui.fragments.feed.feed_base.IFeedLockerView
import com.topface.topface.ui.fragments.feed.feed_base.IFeedNavigator
import com.topface.topface.ui.fragments.feed.feed_utils.addAllFirst
import com.topface.topface.ui.fragments.feed.feed_utils.addFirst
import com.topface.topface.ui.fragments.feed.feed_utils.getFirst
import com.topface.topface.utils.RunningStateManager
import com.topface.topface.utils.Utils
import com.topface.topface.utils.config.FeedsCache
import com.topface.topface.utils.debug.FuckingVoodooMagic
import com.topface.topface.utils.extensions.registerReceiver
import com.topface.topface.utils.extensions.unregisterReceiver
import com.topface.topface.utils.gcmutils.GCMUtils
import com.topface.topface.utils.rx.safeUnsubscribe
import com.topface.topface.utils.rx.shortSubscribe
import rx.Observable
import rx.Observer
import rx.Subscriber
import rx.Subscription
import java.util.*
import java.util.concurrent.atomic.AtomicReference

/**
 * База для VM фидов. Все что связано с activity/fragment нужно сетить после бинда
 * и релизить на unbind
 * Created by tiberal on 09.02.17.
 */
abstract class BaseFeedFragmentModel<T : FeedItem>(private val context: Context) :
        BaseViewModel(), SwipeRefreshLayout.OnRefreshListener, RunningStateManager.OnAppChangeStateListener {

    var navigator: IFeedNavigator? = null
    var api: FeedApi? = null
    var stubView: IFeedLockerView? = null

    private val mState by lazy {
        App.getAppComponent().appState()
    }
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
    private val mUnreadState = FeedRequest.UnreadStatePair(true, false)

    abstract val feedsType: FeedsCache.FEEDS_TYPE
    abstract val itemClass: Class<T>
    abstract val service: FeedRequest.FeedService
    abstract val gcmType: Array<Int>
    open val typeFeedFragment: String? = null
    open val gcmTypeUpdateAction: String? = null
    open val isForPremium: Boolean = false
    open val isNeedReadItems: Boolean = false
    open val isNeedCacheItems: Boolean = true

    abstract fun isCountersChanged(newCounters: CountersData, currentCounters: CountersData): Boolean
    open val bannerRes: Int = R.layout.app_day_list
    private val mCounters by lazy {
        CountersData()
    }
    private val mCache by lazy {
        FeedCacheManager<T>(context, feedsType)
    }
    private var mAppDayRequestSubscription: Subscription? = null
    private var mUpdaterSubscription: Subscription? = null
    private var mDeleteSubscription: Subscription? = null
    private var mBlackListSubscription: Subscription? = null
    private var mCountersSubscription: Subscription? = null

    private val mAtomicUpdaterSubscription = AtomicReference<Subscription>()

    private var isDataFromCache = false
    private var mIsAllDataLoaded = false
    private var mStateManager = RunningStateManager()

    val data = ImprovedObservableList<T>()

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

    companion object {
        const val FROM = "from"
        const val TO = "to"
        const val SERVICE = "service"
        const val LEAVE = "leave"
        const val HISTORY_LOAD_FLAG = "history_load_flag"
        const val PULL_TO_REF_FLAG = "pull_to_refresh_flag"
        const val UNREAD_STATE = "unread_state"
    }

    private lateinit var mReadItemReceiver: BroadcastReceiver
    private lateinit var mGcmReceiver: BroadcastReceiver

    init {
        Debug.log("NEW_BASE new view model -> ${this}")
        if (isNeedCacheItems) {
            mCache.restoreFromCache(itemClass)?.let {
                if (data.isEmpty()) {
                    data.addAll(it)
                    isDataFromCache = true
                }
            }
        } else {
            isListVisible.set(View.INVISIBLE)
        }
        mCountersSubscription = mState.getObservable(CountersData::class.java)
                .filter { newCounters ->
                    val isChanged = mCounters.isNotEmpty && isCountersChanged(newCounters, mCounters)
                    mCounters.setCounters(newCounters)
                    isChanged
                }
                .shortSubscribe { newCounters ->
                    newCounters?.let {
                        loadTopFeeds()
                    }
                }

        createAndRegisterBroadcasts()
        mStateManager.registerAppChangeStateListener(this)
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
        mReadItemReceiver.registerReceiver(context, filter)
        gcmTypeUpdateAction?.let {
            mGcmReceiver.registerReceiver(context, IntentFilter(it))
        }
    }

    open fun makeItemReadWithFeedId(id: String) {
        data.forEachIndexed { position, dataItem ->
            if (TextUtils.equals(dataItem.id, id) && dataItem.unread) {
                dataItem.unread = false
            }
            data[position] = dataItem
        }
    }

    protected fun makeItemReadUserId(uid: Int, readMessages: Int) {
        data.forEachIndexed { position, dataItem ->
            if (dataItem.user != null && dataItem.user.id == uid && dataItem.unread) {
                val unread = dataItem.unreadCounter - readMessages
                if (unread > 0) {
                    dataItem.unreadCounter = unread
                } else {
                    dataItem.unread = false
                    dataItem.unreadCounter = 0
                }
                data[position] = dataItem
            }
        }
    }

    fun update(updateBundle: Bundle = Bundle(), force: Boolean = false) {
        if (mAtomicUpdaterSubscription.get() == null && isDataFromCache ||
                mAtomicUpdaterSubscription.get()?.isUnsubscribed ?: false &&
                        updateBundle.getString(TO, Utils.EMPTY) != Utils.EMPTY || force) {
            isFeedProgressBarVisible.set(View.VISIBLE)
            mAtomicUpdaterSubscription.set(api?.callFeedUpdate(isForPremium, itemClass,
                    constructFeedRequestArgs(isPullToRef = false, to = updateBundle.getString(TO, Utils.EMPTY)))?.
                    subscribe(object : Observer<FeedListData<T>> {
                        override fun onCompleted() {
                            mAtomicUpdaterSubscription.get().safeUnsubscribe()
                            isFeedProgressBarVisible.set(View.INVISIBLE)
                        }

                        override fun onError(e: Throwable?) {
                            mAtomicUpdaterSubscription.get().safeUnsubscribe()
                            e?.let {
                                onErrorProcess(it)
                            }
                            isFeedProgressBarVisible.set(View.INVISIBLE)
                        }

                        override fun onNext(data: FeedListData<T>?) {
                            if (data?.more == false) mIsAllDataLoaded = true
                            updateFeedsLoaded(data, updateBundle)
                        }
                    }))

        }
    }

    protected open fun updateFeedsLoaded(newData: FeedListData<T>?, updateBundle: Bundle) {
        if (isDataFromCache || (data.isEmpty() && !(newData?.items?.isEmpty() ?: true))) {
            typeFeedFragment?.let { getAppDayRequest(it) }
        }
        newData?.let {
            if (isDataFromCache) {
                data.clear()
                isDataFromCache = false
            }
            handleUnreadState(it, updateBundle.getBoolean(PULL_TO_REF_FLAG))
            if (data.isEmpty() && it.items.isEmpty()) {
                isListVisible.set(View.INVISIBLE)
                stubView?.onEmptyFeed()
            } else {
                isListVisible.set(View.VISIBLE)
                isLockViewVisible.set(View.GONE)
                stubView?.onFilledFeed()
            }
            data.addAll(it.items)
        }
    }

    fun getAppDayRequest(typeFeedFragment: String) {
        mAppDayRequestSubscription = api?.callAppDayRequest(typeFeedFragment)?.subscribe(object : Subscriber<AppDay>() {
            override fun onCompleted() {
            }

            override fun onError(e: Throwable?) =
                    e?.let { Debug.log("App day banner error request: $it") } ?: Unit

            override fun onNext(appDay: AppDay?) = appDay?.list?.let { imageArray ->
                if (!imageArray.isEmpty()) {
                    data.addFirst(AppDayStubItem(appDay) as T)
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
                if (data.isEmpty()) {
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

    open fun itemClick(view: View?, itemPosition: Int, data: T?) = navigator?.showChat(data)

    override fun onRefresh() {
        isRefreshing.set(true)
        loadTopFeeds()
    }

    open fun onRefreshed() {
    }

    fun loadTopFeeds() {
        val from = data.getFirst()?.id ?: Utils.EMPTY
        val requestBundle = constructFeedRequestArgs(from = from, to = null)
        mAtomicUpdaterSubscription.set(api?.callFeedUpdate(isForPremium, itemClass, requestBundle)?.
                subscribe(object : Subscriber<FeedListData<T>>() {
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

                }))
    }

    protected open fun topFeedsLoaded(topFeedsData: FeedListData<T>?, requestBundle: Bundle) {
        topFeedsData?.let {
            if (!it.items.isEmpty()) {
                if (this@BaseFeedFragmentModel.data.count() == 0) {
                    isListVisible.set(View.VISIBLE)
                    isLockViewVisible.set(View.GONE)
                    stubView?.onFilledFeed()
                }
                handleUnreadState(it, requestBundle.getBoolean(PULL_TO_REF_FLAG))
                removeOldDuplicates(it)
                data.addAllFirst(it.items)
                this@BaseFeedFragmentModel.data.addAll(0, it.items)
                if (isRefreshing.get()) {
                    isRefreshing.set(false)
                }
            }
        }
    }

    @FuckingVoodooMagic(description = "Если нет новых фидов сервер присылает итем от которого проходила выборка(первый)")
    protected fun removeOldDuplicates(data: FeedListData<T>) = with(this@BaseFeedFragmentModel.data) {
        val feedsIterator = iterator()
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

    private fun hasData() = data.isNotEmpty()

    fun onDeleteFeedItems(items: MutableList<T>, idsList: ArrayList<String>) = api?.let {
        val tempItems = items.toList()
        isLockViewVisible.set(View.VISIBLE)
        mDeleteSubscription = it.callDelete(feedsType, idsList).subscribe(createSubscriber(tempItems))
    }

    fun onAddToBlackList(items: MutableList<T>) = api?.let {
        val tempItems = items.toList()
        isLockViewVisible.set(View.VISIBLE)
        mBlackListSubscription = it.callAddToBlackList(items).subscribe(createSubscriber(tempItems))
    }

    private fun createSubscriber(items: List<T>) = object : Subscriber<Boolean>() {
        override fun onCompleted() = isLockViewVisible.set(View.GONE)

        override fun onError(e: Throwable?) {
            Utils.showErrorMessage()
            isLockViewVisible.set(View.GONE)
        }

        override fun onNext(result: Boolean?) {
            val iterator = data.observableList.listIterator()
            var item: T
            while (iterator.hasNext()) {
                item = iterator.next()
                if (items.contains(item)) {
                    iterator.remove()
                }
            }
            if (data.isEmpty()) {
                isListVisible.set(View.INVISIBLE)
                stubView?.onEmptyFeed()
            }
            mCache.saveToCache(ArrayList<T>(data as List<T>))
        }
    }

    override fun onAppBackground(timeOnStop: Long, timeOnStart: Long) = mGcmReceiver.unregisterReceiver(context)

    override fun onAppForeground(timeOnStart: Long) {
        gcmTypeUpdateAction?.let {
            mGcmReceiver.registerReceiver(context, IntentFilter(it))
        }
        for (type in gcmType) {
            GCMUtils.cancelNotification(context, type)
        }
    }

    override fun unbind() {
        api = null
        navigator = null
        stubView = null
        mAtomicUpdaterSubscription.get().safeUnsubscribe()
        updateObservable = null
    }

    override fun release() {
        unbind()
        arrayOf(mReadItemReceiver, mGcmReceiver).unregisterReceiver(context)
        arrayOf(mUpdaterSubscription, mDeleteSubscription, mBlackListSubscription, mCountersSubscription,
                mAppDayRequestSubscription).safeUnsubscribe()
        if (isNeedCacheItems) {
            if (data.isNotEmpty()) {
                mCache.saveToCache(ArrayList<T>(data as List<T>))
            }
        }
        mStateManager.unregisterAppChangeStateListener(this)
    }
}
