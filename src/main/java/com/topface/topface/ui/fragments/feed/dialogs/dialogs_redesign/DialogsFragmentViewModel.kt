package com.topface.topface.ui.fragments.feed.dialogs.dialogs_redesign

import android.content.Context
import android.content.Intent
import android.databinding.ObservableBoolean
import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.text.TextUtils
import com.topface.topface.App
import com.topface.topface.data.FeedDialog
import com.topface.topface.data.FeedListData
import com.topface.topface.data.History
import com.topface.topface.requests.FeedRequest
import com.topface.topface.state.EventBus
import com.topface.topface.ui.ChatActivity
import com.topface.topface.ui.fragments.ChatFragment
import com.topface.topface.ui.fragments.feed.dialogs.FeedPushHandler
import com.topface.topface.ui.fragments.feed.dialogs.IFeedPushHandlerListener
import com.topface.topface.ui.fragments.feed.feed_api.FeedApi
import com.topface.topface.ui.fragments.feed.feed_base.BaseFeedFragmentViewModel
import com.topface.topface.ui.fragments.feed.feed_utils.getFirstItem
import com.topface.topface.ui.fragments.feed.feed_utils.isEmpty
import com.topface.topface.utils.DateUtils
import com.topface.topface.utils.ILifeCycle
import com.topface.topface.utils.RxUtils
import com.topface.topface.utils.Utils
import com.topface.topface.utils.databinding.SingleObservableArrayList
import com.topface.topface.utils.extensions.safeUnsubscribe
import rx.Observable
import rx.Subscriber
import rx.Subscription
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

/**
 * VM for new and improved dialogs
 * Created by tiberal on 30.11.16.
 */
class DialogsFragmentViewModel(context: Context, private val mApi: FeedApi,
                               updater: () -> Observable<Bundle>)
    : SwipeRefreshLayout.OnRefreshListener, ILifeCycle, IFeedPushHandlerListener {

    var isRefreshing = ObservableBoolean()
    var isEnable = ObservableBoolean(true)

    private var mCallUpdateSubscription: Subscription? = null
    private var mIsAllDataLoaded: Boolean = false
    private val mUnreadState = FeedRequest.UnreadStatePair(true, false)
    private val mPushHandler = FeedPushHandler(this, context)
    private var isTopFeedsLoading = AtomicBoolean(false)

    @Inject lateinit var mEventBus: EventBus

    val data = SingleObservableArrayList<FeedDialog>()

    private var mContentAvailableSubscription: Subscription
    private var mUpdaterSubscription: Subscription

    init {
        App.get().inject(this)
        mUpdaterSubscription = updater().distinct {
            it?.getString(BaseFeedFragmentViewModel.TO, Utils.EMPTY)
        }.subscribe(object : RxUtils.ShortSubscription<Bundle>() {
            override fun onNext(updateBundle: Bundle?) {
                if (!mIsAllDataLoaded) {
                    updateBundle?.let {
                        update(it)
                    }
                }
            }
        })
        mContentAvailableSubscription = Observable.zip(mEventBus.getObservable(DialogContactsEvent::class.java),
                (mEventBus.getObservable(DialogItemsEvent::class.java))) { item1, item2 ->
            item1.hasContacts || item2.hasDialogItems
        }.first().filter { !it }.subscribe(object : RxUtils.ShortSubscription<Boolean>() {
            override fun onNext(type: Boolean?) {
                data.observableList.clear()
                isEnable.set(false)
                data.observableList.add(EmptyDialogsFragmentStubItem())
            }
        })


    }

    override fun onResume() {
        if (mCallUpdateSubscription?.isUnsubscribed ?: true && data.observableList.isEmpty()) {
            update()
        }
    }

    private fun handleUnreadState(data: FeedListData<FeedDialog>, isPullToRef: Boolean) {
        if (!data.items.isEmpty()) {
            if (!mUnreadState.wasFromInited || isPullToRef) {
                mUnreadState.from = data.items.first.unread
                mUnreadState.wasFromInited = true
            }
            mUnreadState.to = data.items.last.unread
        }
    }

    private fun update(updateBundle: Bundle = Bundle()) {
        mCallUpdateSubscription = mApi.callFeedUpdate(false, FeedDialog::class.java,
                constructFeedRequestArgs(isPullToRef = false, to = updateBundle.getString(BaseFeedFragmentViewModel.TO, Utils.EMPTY))).
                subscribe(object : RxUtils.ShortSubscription<FeedListData<FeedDialog>>() {
                    override fun onCompleted() {
                        mCallUpdateSubscription.safeUnsubscribe()
                    }

                    override fun onNext(data: FeedListData<FeedDialog>?) {
                        data?.let {
                            addContactsItem()
                            mEventBus.setData(DialogItemsEvent(data.items.isNotEmpty()))
                            if (it.items.isEmpty()) {
                                this@DialogsFragmentViewModel.data.observableList.add(EmptyDialogsStubItem())
                            } else {
                                this@DialogsFragmentViewModel.data.addAll(it.items)
                                handleUnreadState(it, false)
                                mIsAllDataLoaded = !data.more
                            }
                        }
                    }
                })
    }

    private fun addContactsItem() {
        if (data.observableList.isEmpty() && this@DialogsFragmentViewModel.data.observableList.isEmpty()) {
            data.observableList.add(DialogContactsStubItem())
        }
    }

    fun loadTopFeeds() {
        if (isTopFeedsLoading.get()) return
        isTopFeedsLoading.set(true)
        val from = data.observableList.getFirstItem()?.id ?: return
        val requestBundle = constructFeedRequestArgs(from = from, to = null)
        mCallUpdateSubscription = mApi.callFeedUpdate(false, FeedDialog::class.java, requestBundle)
                .subscribe(object : Subscriber<FeedListData<FeedDialog>>() {
                    override fun onCompleted() {
                        mCallUpdateSubscription.safeUnsubscribe()
                        isTopFeedsLoading.set(false)
                        if (isRefreshing.get()) {
                            isRefreshing.set(false)
                        }
                    }

                    override fun onError(e: Throwable?) {
                        isTopFeedsLoading.set(false)
                        if (isRefreshing.get()) {
                            isRefreshing.set(false)
                        }
                    }

                    override fun onNext(data: FeedListData<FeedDialog>?) = with(this@DialogsFragmentViewModel.data.observableList) {
                        if (data != null && data.items.isNotEmpty()) {
                            /*
                             Первый итем для контактов, второй для заглушки(если нет диалогов)
                             */
                            if (count() == 2 && this[1].isEmpty()) {
                                //удаляем заглушку
                                removeAt(1)
                            }
                            data.items.forEach { topDialog ->
                                val iterator = this@DialogsFragmentViewModel.data.observableList.listIterator()
                                while (iterator.hasNext()) {
                                    val item = iterator.next()
                                    if (item.user != null && item.user.id == topDialog.user.id) {
                                        iterator.remove()
                                    }
                                }
                            }
                            if (data.items.isNotEmpty()) {
                                addAll(1, data.items)
                            }
                        }
                    }
                })
    }

    /**
     * Апдейтит итем диалога
     * @param oldItem итем который надо апдейтить
     * @param newItem новый итем от которого берем инфу для апдейта
     * @param targetItemPosition позиция итема для апдейта в массиве
     */
    private fun updateDialogPreview(oldItem: FeedDialog, newItem: FeedDialog, targetItemPosition: Int) {
        val tempItem = oldItem
        tempItem.type = newItem.type
        tempItem.text = newItem.text
        tempItem.target = newItem.target
        tempItem.createdRelative = DateUtils.getRelativeDate(newItem.created, true)
        tempItem.unread = newItem.unread
        this@DialogsFragmentViewModel.data.observableList[targetItemPosition] = tempItem
    }

    /**
     * Контейнер с данными для запроса
     */
    private fun constructFeedRequestArgs(isPullToRef: Boolean = true, from: String? = Utils.EMPTY,
                                         to: String? = Utils.EMPTY) =
            Bundle().apply {
                putSerializable(BaseFeedFragmentViewModel.SERVICE, FeedRequest.FeedService.DIALOGS)
                putParcelable(BaseFeedFragmentViewModel.UNREAD_STATE, mUnreadState)
                putBoolean(BaseFeedFragmentViewModel.PULL_TO_REF_FLAG, isPullToRef)
                putString(BaseFeedFragmentViewModel.FROM, from)
                putString(BaseFeedFragmentViewModel.TO, to)
                putBoolean(BaseFeedFragmentViewModel.HISTORY_LOAD_FLAG, !data.observableList.isEmpty())
            }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        data?.let {
            if (requestCode == ChatActivity.REQUEST_CHAT) {
                tryUpdatePreview(it)
                if (data.getBooleanExtra(ChatFragment.SEND_MESSAGE, false)) {
                    loadTopFeeds()
                }
            }
        }
    }

    /**
     * Пытаемся обновить инфу в итеме диалога
     * @param intent данные из onActivityResult
     */
    fun tryUpdatePreview(intent: Intent) {
        val history = intent.getParcelableExtra<History>(ChatActivity.LAST_MESSAGE)
        val userId = intent.getIntExtra(ChatActivity.LAST_MESSAGE_USER_ID, -1)
        if (history != null && userId > 0) {
            data.observableList.forEachIndexed { position, item ->
                if (item.user != null && item.user.id == userId) {
                    updateDialogPreview(item, history, position)
                }
            }
        }
    }

    fun release() {
        arrayOf(mCallUpdateSubscription, mContentAvailableSubscription, mUpdaterSubscription).safeUnsubscribe()
        mPushHandler.release()
        data.removeListener()
    }

    override fun onRefresh() {
        isRefreshing.set(true)
        loadTopFeeds()
    }

    override fun updateFeedDialogs() = loadTopFeeds()


    override fun makeItemReadWithFeedId(itemId: String) {
        var itemForRead: FeedDialog
        data.observableList.forEachIndexed { position, dataItem ->
            if (TextUtils.equals(dataItem.id, itemId) && dataItem.unread) {
                itemForRead = dataItem
                // data.observableList.remove(dataItem)
                itemForRead.unread = false
                data.observableList[position] = itemForRead
                return
            }
        }
    }

    override fun makeItemReadUserId(userId: Int, readMessages: Int) {
        var itemForRead: FeedDialog
        data.observableList.forEachIndexed { position, dataItem ->
            if (dataItem.user != null && dataItem.user.id == userId && dataItem.unread) {
                itemForRead = dataItem
                // data.observableList.remove(dataItem)
                val unread = dataItem.unreadCounter - readMessages
                if (unread > 0) {
                    itemForRead.unreadCounter = unread
                } else {
                    itemForRead.unread = false
                    itemForRead.unreadCounter = 0
                }
                data.observableList[position] = itemForRead
            }
        }
    }

}