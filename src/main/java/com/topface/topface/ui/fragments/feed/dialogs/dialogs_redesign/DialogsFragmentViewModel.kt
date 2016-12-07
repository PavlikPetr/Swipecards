package com.topface.topface.ui.fragments.feed.dialogs.dialogs_redesign

import android.content.Context
import android.content.Intent
import android.databinding.ObservableBoolean
import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.text.TextUtils
import com.topface.topface.data.FeedDialog
import com.topface.topface.data.FeedItem
import com.topface.topface.data.FeedListData
import com.topface.topface.requests.FeedRequest
import com.topface.topface.ui.ChatActivity
import com.topface.topface.ui.fragments.ChatFragment
import com.topface.topface.ui.fragments.feed.dialogs.FeedPushHandler
import com.topface.topface.ui.fragments.feed.dialogs.IFeedPushHandlerListener
import com.topface.topface.ui.fragments.feed.feed_api.FeedApi
import com.topface.topface.ui.fragments.feed.feed_base.BaseFeedFragmentViewModel
import com.topface.topface.ui.fragments.feed.feed_utils.getFirstItem
import com.topface.topface.ui.fragments.feed.feed_utils.isEmpty
import com.topface.topface.utils.ILifeCycle
import com.topface.topface.utils.RxUtils
import com.topface.topface.utils.Utils
import com.topface.topface.utils.databinding.SingleObservableArrayList
import com.topface.topface.utils.extensions.safeUnsubscribe
import rx.Observable
import rx.Subscriber
import rx.Subscription

/**
 * VM for new and improved dialogs
 * Created by tiberal on 30.11.16.
 */
class DialogsFragmentViewModel(context: Context, private val mApi: FeedApi,
                               updater: () -> Observable<Bundle>)
    : SwipeRefreshLayout.OnRefreshListener, ILifeCycle, IFeedPushHandlerListener {

    var isRefreshing = ObservableBoolean()

    private var mCallUpdateSubscription: Subscription? = null
    private var mIsAllDataLoaded: Boolean = false
    private val mUnreadState = FeedRequest.UnreadStatePair(true, false)
    private val mPushHandler = FeedPushHandler(this, context)

    val data = SingleObservableArrayList<FeedItem>()

    init {
        updater().distinct {
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

    //todo проверить, что оправляется именно последний ид фида
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
        val from = data.observableList.getFirstItem()?.id ?: return
        val requestBundle = constructFeedRequestArgs(from = from, to = null)
        mCallUpdateSubscription = mApi.callFeedUpdate(false, FeedDialog::class.java, requestBundle)
                .subscribe(object : Subscriber<FeedListData<FeedDialog>>() {
                    override fun onCompleted() {
                        if (isRefreshing.get()) {
                            isRefreshing.set(false)
                        }
                    }

                    override fun onError(e: Throwable?) {
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
                            addAll(0, data.items)
                        }
                    }

                })
    }


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
            if (requestCode == ChatActivity.REQUEST_CHAT
                    && data.getBooleanExtra(ChatFragment.SEND_MESSAGE, false)) {
                loadTopFeeds()
            }
        }
    }

    fun release() {
        mCallUpdateSubscription.safeUnsubscribe()
        data.removeListener()
    }

    override fun onRefresh() {
        isRefreshing.set(true)
        loadTopFeeds()
    }

    override fun updateFeedDialogs() {
        loadTopFeeds()
    }

    override fun updateFeedMutual() {

    }

    override fun updateFeedAdmiration() {
    }

    override fun makeItemReadWithFeedId(itemId: String) {
        var itemForRead: FeedItem
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
        var itemForRead: FeedItem
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
                data.observableList
                data.observableList[position] = itemForRead
            }
        }
    }

}