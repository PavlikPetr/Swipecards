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
import com.topface.scruffy.data.ApiError
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.api.FeedRequestFactory
import com.topface.topface.api.IApi
import com.topface.topface.api.UnreadStatePair
import com.topface.topface.api.responses.Completed
import com.topface.topface.api.responses.IBaseFeedResponse
import com.topface.topface.data.CountersData
import com.topface.topface.data.FeedItem
import com.topface.topface.requests.handlers.ErrorCodes
import com.topface.topface.ui.fragments.ChatFragment
import com.topface.topface.ui.fragments.feed.dialogs.dialogs_redesign.AppDayStubItem
import com.topface.topface.ui.fragments.feed.enhanced.base.BaseFeedLockerController.Companion.EMPTY_FEED
import com.topface.topface.ui.fragments.feed.enhanced.base.BaseFeedLockerController.Companion.FILLED_FEED
import com.topface.topface.ui.fragments.feed.enhanced.base.BaseFeedLockerController.Companion.LOCKED_FEED
import com.topface.topface.ui.fragments.feed.enhanced.base.BaseFeedLockerController.Companion.NONE
import com.topface.topface.ui.fragments.feed.enhanced.utils.ImprovedObservableList
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
import com.topface.topface.utils.rx.shortSubscription
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
typealias LockerStubLastState = Pair<Long, Int>

abstract class BaseFeedFragmentModel<T : FeedItem>(private val mContext: Context, private val mApi: IApi) :
        BaseViewModel(), SwipeRefreshLayout.OnRefreshListener, RunningStateManager.OnAppChangeStateListener {

    var navigator: IFeedNavigator? = null
    var stubView: IFeedLockerView? = null
        set(value) {
            field = value
            switchLockerStubView(lockerStubLastState.first, field, lockerStubLastState.second)
        }

    private var lockerStubLastState = LockerStubLastState(NONE, -666)

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
    private val mUnreadState = UnreadStatePair(true, false)

    abstract val feedsType: FeedsCache.FEEDS_TYPE
    abstract val itemClass: Class<T>
    abstract val responseClass: Class<out IBaseFeedResponse>
    abstract val service: FeedRequestFactory.FeedService
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
        FeedCacheManager<T>(mContext, feedsType)
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
                }
            }
            isDataFromCache = true
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
        mReadItemReceiver.registerReceiver(mContext, filter)
        gcmTypeUpdateAction?.let {
            mGcmReceiver.registerReceiver(mContext, IntentFilter(it))
        }
    }

    private fun switchLockerStubView(@LockerStubState state: Long, stubView: IFeedLockerView?,
                                     errorCode: Int = -666) = stubView?.let {
        lockerStubLastState = LockerStubLastState(state, errorCode)
        when (state) {
            FILLED_FEED -> it.onFilledFeed()
            EMPTY_FEED -> it.onEmptyFeed()
            LOCKED_FEED -> it.onLockedFeed(errorCode)
            else -> {
                lockerStubLastState = LockerStubLastState(NONE, errorCode)
            }
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
        if (mAtomicUpdaterSubscription.get() == null && (!isNeedCacheItems || isDataFromCache) ||
                mAtomicUpdaterSubscription.get()?.isUnsubscribed ?: false &&
                        updateBundle.getString(TO, Utils.EMPTY) != Utils.EMPTY || force) {
            isFeedProgressBarVisible.set(View.VISIBLE)
            val arg = constructFeedRequestArgs(isPullToRef = false, to = updateBundle.getString(TO, Utils.EMPTY))
            mAtomicUpdaterSubscription.set(mApi.callGetList(arg, responseClass, itemClass).
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

    protected open fun updateFeedsLoaded(newData: ArrayList<out FeedItem>, updateBundle: Bundle) {
        if (isDataFromCache || (data.isEmpty() && !(newData.isEmpty()))) {
            typeFeedFragment?.let { getAppDayRequest(it) }
        }
        if (isDataFromCache) {
            data.clear()
            isDataFromCache = false
        }
        handleUnreadState(newData, updateBundle.getBoolean(PULL_TO_REF_FLAG))
        if (data.isEmpty() && newData.isEmpty()) {
            isListVisible.set(View.INVISIBLE)
            switchLockerStubView(EMPTY_FEED, stubView)
        } else {
            isListVisible.set(View.VISIBLE)
            isLockViewVisible.set(View.GONE)
            switchLockerStubView(FILLED_FEED, stubView)
        }
        (newData as? ArrayList<T>)?.let {
            data.addAll(it)
        }
    }

    fun getAppDayRequest(typeFeedFragment: String) {
        mAppDayRequestSubscription = mApi.callAppDayRequest(typeFeedFragment)
                .subscribe(shortSubscription {
                    it?.list?.let { imageArray ->
                        if (!imageArray.isEmpty()) {
                            data.addFirst(AppDayStubItem(it) as T)
                        }
                    }
                })
    }

    private fun onErrorProcess(apiError: ApiError?) = apiError?.let {
        it.printStackTrace()
        val codeError = Integer.valueOf(it.code)
        when (codeError) {
            ErrorCodes.PREMIUM_ACCESS_ONLY, ErrorCodes.BLOCKED_SYMPATHIES
                , ErrorCodes.BLOCKED_PEOPLE_NEARBY -> {
                isListVisible.set(View.INVISIBLE)
                isFeedProgressBarVisible.set(View.INVISIBLE)
                switchLockerStubView(LOCKED_FEED, stubView, codeError)
                return@let
            }
            else -> {
                if (data.isEmpty()) {
                    isFeedProgressBarVisible.set(View.VISIBLE)
                }
                switchLockerStubView(FILLED_FEED, stubView)
            }
        }
    }

    private fun handleUnreadState(data: ArrayList<out FeedItem>, isPullToRef: Boolean) {
        if (!data.isEmpty()) {
            if (!mUnreadState.wasFromInited || isPullToRef) {
                mUnreadState.from = data.first().unread
                mUnreadState.wasFromInited = true
            }
            mUnreadState.to = data.last().unread
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
        mAtomicUpdaterSubscription.set(mApi.callGetList(requestBundle, responseClass, itemClass)
                .subscribe(object : Observer<IBaseFeedResponse> {
                    override fun onCompleted() {
                        mAtomicUpdaterSubscription.get().safeUnsubscribe()
                        if (isRefreshing.get()) {
                            isRefreshing.set(false)
                        }
                    }

                    override fun onError(e: Throwable?) {
                        mAtomicUpdaterSubscription.get().safeUnsubscribe()
                        onErrorProcess(e as? ApiError)
                        if (isRefreshing.get()) {
                            isRefreshing.set(false)
                        }
                    }

                    override fun onNext(data: IBaseFeedResponse?) = data?.let {
                        topFeedsLoaded(data.getItemsList(), requestBundle)
                    } ?: Unit
                }))
    }

    protected open fun topFeedsLoaded(topFeedsData: ArrayList<out FeedItem>, requestBundle: Bundle) =
            (topFeedsData as? ArrayList<T>)?.let {
                if (!it.isEmpty()) {
                    if (this@BaseFeedFragmentModel.data.count() == 0) {
                        isListVisible.set(View.VISIBLE)
                        isLockViewVisible.set(View.GONE)
                        switchLockerStubView(FILLED_FEED, stubView)
                    }
                    handleUnreadState(it, requestBundle.getBoolean(PULL_TO_REF_FLAG))
                    removeOldDuplicates(it)
                    data.addAllFirst(it)
                    if (isRefreshing.get()) {
                        isRefreshing.set(false)
                    }
                }
            }


    @FuckingVoodooMagic(description = "Если нет новых фидов сервер присылает итем от которого проходила выборка(первый)")
    protected fun removeOldDuplicates(data: ArrayList<T>) = with(this@BaseFeedFragmentModel.data) {
        val feedsIterator = iterator()
        while (feedsIterator.hasNext()) {
            val feed = feedsIterator.next()
            for (newFeed in data) {
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

    private fun createSubscriber(items: List<T>) = object : Subscriber<Completed>() {
        override fun onCompleted() = isLockViewVisible.set(View.GONE)

        override fun onError(e: Throwable?) {
            Utils.showErrorMessage()
            isLockViewVisible.set(View.GONE)
        }

        override fun onNext(result: Completed?) {
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
                switchLockerStubView(EMPTY_FEED, stubView)
            }
            mCache.saveToCache(ArrayList<T>(data as List<T>))
        }
    }

    override fun onAppBackground(timeOnStop: Long, timeOnStart: Long) = mGcmReceiver.unregisterReceiver(mContext)

    override fun onAppForeground(timeOnStart: Long) {
        gcmTypeUpdateAction?.let {
            mGcmReceiver.registerReceiver(mContext, IntentFilter(it))
        }
        for (type in gcmType) {
            GCMUtils.cancelNotification(mContext, type)
        }
    }

    override fun unbind() {
        navigator = null
        stubView = null
        mAtomicUpdaterSubscription.get().safeUnsubscribe()
        updateObservable = null
    }

    override fun release() {
        unbind()
        arrayOf(mReadItemReceiver, mGcmReceiver).unregisterReceiver(mContext)
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
