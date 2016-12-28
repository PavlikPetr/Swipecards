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
import com.topface.topface.ui.fragments.feed.app_day.AppDay
import com.topface.topface.ui.fragments.feed.dialogs.FeedPushHandler
import com.topface.topface.ui.fragments.feed.dialogs.IFeedPushHandlerListener
import com.topface.topface.ui.fragments.feed.feed_api.FeedApi
import com.topface.topface.ui.fragments.feed.feed_base.BaseFeedFragmentViewModel
import com.topface.topface.ui.fragments.feed.feed_utils.getRealDataFirstItem
import com.topface.topface.ui.fragments.feed.feed_utils.isEmpty
import com.topface.topface.utils.DateUtils
import com.topface.topface.utils.ILifeCycle
import com.topface.topface.utils.Utils
import com.topface.topface.utils.databinding.SingleObservableArrayList
import com.topface.topface.utils.rx.RxUtils
import com.topface.topface.utils.rx.safeUnsubscribe
import rx.Observable
import rx.Subscriber
import rx.Subscription
import rx.functions.Func1
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

/**
 * VM for new and improved dialogs
 * Created by tiberal on 30.11.16.
 */
class DialogsFragmentViewModel(context: Context, private val mApi: FeedApi,
                               private val updater: () -> Observable<Bundle>)
    : SwipeRefreshLayout.OnRefreshListener, ILifeCycle, IFeedPushHandlerListener {

    var isRefreshing = ObservableBoolean()
    var isEnable = ObservableBoolean(true)

    private var mLoadTopSubscription: Subscription? = null
    private var mIsAllDataLoaded: Boolean = false
    private val mUnreadState = FeedRequest.UnreadStatePair(true, false)
    private val mPushHandler = FeedPushHandler(this, context)
    private var isTopFeedsLoading = AtomicBoolean(false)

    @Inject lateinit var mEventBus: EventBus

    val data = SingleObservableArrayList<FeedDialog>()

    private var mContentAvailableSubscription: Subscription? = null
    private var mUpdaterSubscription: Subscription? = null
    private var mAppDayRequestSubscription: Subscription? = null
    private var mUpdateFromPopupMenuSubscription: Subscription? = null

    init {
        App.get().inject(this)
        bindUpdater()
        mContentAvailableSubscription = Observable.combineLatest(mEventBus.getObservable(DialogContactsEvent::class.java),
                mEventBus.getObservable(DialogItemsEvent::class.java)) { item1, item2 ->
            if (!item2.hasDialogItems) {
                // add empty dialogs stub if need
                var found = false
                val stub = EmptyDialogsStubItem()
                data.observableList.forEach {
                    if (it.javaClass == stub.javaClass) {
                        found = true
                        return@forEach
                    }
                }
                if (!found) data.observableList.add(stub)
            }
            item1.hasContacts || item2.hasDialogItems
        }
                .filter { !it }
                .subscribe(object : RxUtils.ShortSubscription<Boolean>() {
                    override fun onNext(type: Boolean?) {
                        isEnable.set(false)
                        data.observableList.clear()
                        data.observableList.add(EmptyDialogsFragmentStubItem())
                    }
                })

        // подписка на события об удалении или добавлении  в ч\с через попап меню.
        mUpdateFromPopupMenuSubscription = mEventBus.getObservable(DialogPopupEvent::class.java).subscribe(object : RxUtils.ShortSubscription<DialogPopupEvent>() {
            // если все пришло, то удаляем пришедший итем из списка сообщений через итератор
            override fun onNext(type: DialogPopupEvent?) {
                super.onNext(type)
                if (type != null) {
                    val iterator = data.observableList.listIterator()
                    var item: FeedDialog
                    while (iterator.hasNext()) {
                        item = iterator.next()
                        if (item.user != null) {
                            if (item.user.id == type.feedForDelete.user.id) {
                                iterator.remove()
                            }
                        }
                    }
                }

            }
        })

    }


    private fun bindUpdater() {
        val appDayRequest = mApi.callAppDayRequest(AppDayViewModel.TYPE_FEED_FRAGMENT)
        mUpdaterSubscription = updater().distinct {
            it?.getString(BaseFeedFragmentViewModel.TO, Utils.EMPTY)
        }.filter {
            !mIsAllDataLoaded
        }.flatMap {
            Observable.combineLatest<AppDay, Bundle, Pair<AppDay, Bundle>>(appDayRequest, Observable.just(it)) { appDay, bundle ->
                mEventBus.setData(appDay)
                Pair(appDay, bundle)
            }
        }.flatMap {
            mApi.callFeedUpdate(false, FeedDialog::class.java,
                    constructFeedRequestArgs(isPullToRef = false
                            , to = it.second.getString(BaseFeedFragmentViewModel.TO, Utils.EMPTY))).zipWith(Observable.just(it.first)) {
                list, appDay ->
                Pair(appDay, list)
            }
        }.flatMap(object : Func1<Pair<AppDay, FeedListData<FeedDialog>>, Observable<FeedListData<FeedDialog>>> {
            var insertCount = 0

            override fun call(data: Pair<AppDay, FeedListData<FeedDialog>>): Observable<FeedListData<FeedDialog>> {
                if (insertCount < data.first.maxCount) {
                    val iterator = data.second.items.listIterator()
                    var lastInsertPos = -1
                    while (iterator.hasNext()) {
                        val nextIndex = iterator.nextIndex()
                        val item = iterator.next()
                        if (nextIndex == data.first.firstPosition - 1
                                || (lastInsertPos == nextIndex - (data.first.repeat + 1)
                                && item !is AppDayStubItem && insertCount < data.first.maxCount)) {
                            iterator.add(AppDayStubItem(data.first))
                            insertCount++
                            lastInsertPos = nextIndex
                        }
                    }
                }
                return Observable.just(data.second)
            }

        }).subscribe(
                {
                    it?.let {
                        addContactsItem()
                        with(this@DialogsFragmentViewModel.data) {
                            if (it.items.isEmpty()) {
                                if (isEmptyDialogs()) {
                                    observableList.add(EmptyDialogsStubItem())
                                }
                            } else {
                                addAll(it.items)
                                handleUnreadState(it, false)
                                mIsAllDataLoaded = !it.more
                            }
                            mEventBus.setData(DialogItemsEvent(!isEmptyDialogs()))
                        }
                    }
                }, { it?.printStackTrace() }
        )
    }

    override fun onResume() {
        if (mLoadTopSubscription?.isUnsubscribed ?: true && mUpdaterSubscription?.isUnsubscribed ?: true && data.observableList.isEmpty()) {
            bindUpdater()
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

    private fun addContactsItem() {
        if (data.observableList.isEmpty() && this@DialogsFragmentViewModel.data.observableList.isEmpty()) {
            data.observableList.add(DialogContactsStubItem())
        }
    }

    fun loadTopFeeds() {
        if (isTopFeedsLoading.get()) return
        isTopFeedsLoading.set(true)
        // список диалогов может быть пустой, т.е. в нем нет ничего кроме фейков (заглушка/реклама),
        // в этом случае запросим весь список
        val from = data.observableList.getRealDataFirstItem()?.id ?: ""
        val requestBundle = constructFeedRequestArgs(from = from, to = null)
        mLoadTopSubscription = mApi.callFeedUpdate(false, FeedDialog::class.java, requestBundle)
                .subscribe(object : Subscriber<FeedListData<FeedDialog>>() {
                    override fun onCompleted() {
                        mLoadTopSubscription.safeUnsubscribe()
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
                            } else
                            /*
                             если итем только один и тот заглушка, значит у нас на экране общий стаб
                             чистим список, добавляем стаб пустых контактов
                             диалоги будут добавлены ниже
                             */
                                if (count() == 1 && this[0].isEmpty()) {
                                    clear()
                                    addContactsItem()
                                }
                            // поиск дубликатов в старом списке и удаление их
                            data.items.forEach { topDialog ->
                                val iterator = listIterator()
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
                        mEventBus.setData(DialogItemsEvent(!isEmptyDialogs()))
                    }
                })
    }

    private fun isEmptyDialogs() = this@DialogsFragmentViewModel.data.observableList.getRealDataFirstItem() == null

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
        arrayOf(mLoadTopSubscription, mContentAvailableSubscription,
                mUpdaterSubscription, mAppDayRequestSubscription, mUpdateFromPopupMenuSubscription).safeUnsubscribe()
        mPushHandler.release()
        isRefreshing.set(false)
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
                val unread = dataItem.unreadCounter - readMessages
                if (unread > 0) {
                    itemForRead.unreadCounter = unread
                } else {
                    itemForRead.unread = false
                    itemForRead.unreadCounter = 0
                }
                itemForRead.unread = false
                itemForRead.unreadCounter = 0
                data.observableList[position] = itemForRead
                return
            }
        }
    }

    override fun userAddToBlackList(userId: Int) {
        val iterator = data.observableList.listIterator()
        var item: FeedDialog
        var gotUser = false
        // этом цикле делается две задачи
        // 1 удаляется диалог с пользователем, которого внесли в черный список
        // 2 вычисляется флаг, что остались еще диалоги, помимо всяких заглушек/реклам и тд
        while (iterator.hasNext()) {
            item = iterator.next()
            if (item.user != null) {
                // если нашли пользователя, то либо удалим его (если его внесли в чс)
                // либо запомним что еще есть диалоги
                if (item.user.id == userId) iterator.remove() else gotUser = true
            }
        }
        // если нет диалогов с пользователями, то надо уведомить для добавления заглушки
        if (!gotUser) {
            mEventBus.setData(DialogItemsEvent(false))
        }
    }
}