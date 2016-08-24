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
import com.topface.topface.data.FeedItem
import com.topface.topface.data.FeedListData
import com.topface.topface.databinding.FragmentFeedBaseBinding
import com.topface.topface.requests.FeedRequest
import com.topface.topface.requests.handlers.ErrorCodes
import com.topface.topface.ui.fragments.ChatFragment
import com.topface.topface.ui.fragments.feed.feed_api.FeedApi
import com.topface.topface.ui.fragments.feed.feed_utils.getFirst
import com.topface.topface.utils.RxUtils
import com.topface.topface.utils.Utils
import com.topface.topface.utils.config.FeedsCache
import com.topface.topface.utils.debug.FuckingVoodooMagic
import com.topface.topface.utils.gcmutils.GCMUtils
import com.topface.topface.viewModels.BaseViewModel
import rx.Observer
import rx.Subscriber
import rx.Subscription

/**
 * Базовая VM для всех фидов
 * Created by tiberal on 01.08.16.
 * @param T - feed item type
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
        binding.feedList.adapter as BaseFeedAdapter<*, T>
    }
    abstract val feedsType: FeedsCache.FEEDS_TYPE
    abstract val itemClass: Class<T>
    abstract val service: FeedRequest.FeedService
    abstract val gcmType: Array<Int>
    open val gcmTypeUpdateAction: String? = null
    open val isForPremium: Boolean = false
    open val isNeedReadItems: Boolean = false
    private val mCache by lazy {
        FeedCacheManager<T>(context, feedsType)
    }
    private var mCallUpdateSubscription: Subscription? = null
    private var mUpdaterSubscription: Subscription? = null
    private var mDeleteSubscription: Subscription? = null
    private var mBlackListSubscription: Subscription? = null

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
        mCache.restoreFromCache(itemClass)?.let {
            if (mAdapter.data.isEmpty()) {
                mAdapter.addData(it)
                isDataFromCache = true
            }
        }
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
    private fun createAndRegisterBroadcasts() {
        mGcmReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                for (type in gcmType) {
                    GCMUtils.cancelNotification(context, type)
                }
                onRefresh()
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

    protected fun makeItemReadWithFeedId(id: String) = mAdapter.data.forEachIndexed { position, dataItem ->
        if (TextUtils.equals(dataItem.id, id) && dataItem.unread) {
            dataItem.unread = false
        }
        mAdapter.notifyItemChanged(position)
    }


    protected fun makeItemReadUserId(uid: Int, readMessages: Int) =
            mAdapter.data.forEachIndexed { position, dataItem ->
                if (dataItem.user != null && dataItem.user.id == uid && dataItem.unread) {
                    val unread = dataItem.unreadCounter - readMessages
                    if (unread > 0) {
                        dataItem.unreadCounter = unread
                    } else {
                        dataItem.unread = false
                    }
                    mAdapter.notifyItemChanged(position)
                }
            }

    fun update(updateBundle: Bundle = Bundle()) {
        isFeedProgressBarVisible.set(View.VISIBLE)
        mCallUpdateSubscription = mApi.callUpdate(isForPremium, itemClass,
                constructFeedRequestArgs(isPullToRef = false, to = updateBundle.getString(TO, Utils.EMPTY))).
                subscribe(object : Observer<FeedListData<T>> {
                    override fun onCompleted() {
                        RxUtils.safeUnsubscribe(mCallUpdateSubscription)
                        isFeedProgressBarVisible.set(View.INVISIBLE)
                    }

                    override fun onError(e: Throwable?) {
                        e?.let {
                            onErrorProcess(it)
                        }
                    }

                    override fun onNext(data: FeedListData<T>?) {
                        data?.let {
                            if (isDataFromCache) {
                                mAdapter.data.clear()
                                mAdapter.notifyDataSetChanged()
                                isDataFromCache = false
                            }
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
        mCallUpdateSubscription = mApi.callUpdate(isForPremium, itemClass, requestBundle)
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
                                mAdapter.addFirst(data.items)
                                binding.feedList.layoutManager.scrollToPosition(0)
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
    protected fun considerDuplicates(first: T, second: T) = if (first.id == null) second.id == null else first.id == second.id

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
        RxUtils.safeUnsubscribe(mCallUpdateSubscription)
        RxUtils.safeUnsubscribe(mDeleteSubscription)
        RxUtils.safeUnsubscribe(mBlackListSubscription)
        if (!mAdapter.data.isEmpty()) {
            mCache.saveToCache(mAdapter.data)
        }
        LocalBroadcastManager.getInstance(context).unregisterReceiver(mReadItemReceiver)
        LocalBroadcastManager.getInstance(context).unregisterReceiver(mGcmReceiver)
    }
}
