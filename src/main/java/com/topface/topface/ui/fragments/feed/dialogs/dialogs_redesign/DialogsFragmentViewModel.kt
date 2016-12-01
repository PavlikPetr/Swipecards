package com.topface.topface.ui.fragments.feed.dialogs.dialogs_redesign

import android.databinding.ObservableBoolean
import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import com.topface.topface.data.FeedDialog
import com.topface.topface.data.FeedItem
import com.topface.topface.data.FeedListData
import com.topface.topface.requests.FeedRequest
import com.topface.topface.ui.fragments.feed.feed_api.FeedApi
import com.topface.topface.ui.fragments.feed.feed_base.BaseFeedFragmentViewModel
import com.topface.topface.ui.fragments.feed.feed_base.IFeedNavigator
import com.topface.topface.ui.fragments.feed.feed_utils.getFirst
import com.topface.topface.ui.fragments.feed.feed_utils.isEmpty
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
class DialogsFragmentViewModel(private val mNavigator: IFeedNavigator, private val mApi: FeedApi,
                               updater: () -> Observable<Bundle>) : SwipeRefreshLayout.OnRefreshListener {

    var isRefreshing = ObservableBoolean()

    private var mCallUpdateSubscription: Subscription? = null
    private var mIsAllDataLoaded: Boolean = false
    private val mUnreadState = FeedRequest.UnreadStatePair(true, false)

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
                    override fun onCompleted() = mCallUpdateSubscription.safeUnsubscribe()
                    override fun onNext(data: FeedListData<FeedDialog>?) {
                        data?.let {
                            if (it.items.isEmpty()) {
                                this@DialogsFragmentViewModel.data.observableList.add(EmptyDialogsItem())
                            } else {
                                this@DialogsFragmentViewModel.data.addAll(it.items)
                                handleUnreadState(it, false)
                                mIsAllDataLoaded = !data.more
                            }
                        }
                    }
                })
    }

    fun loadTopFeeds() {
        val from = data.observableList.getFirst()?.id ?: Utils.EMPTY
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
                        if (data != null && data.items.isNotEmpty() && count() == 1 && this[0].isEmpty()) {
                            removeAt(0)
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

    fun release() {
        mCallUpdateSubscription.safeUnsubscribe()
        data.removeListener()
    }

    override fun onRefresh() {
        isRefreshing.set(true)
        loadTopFeeds()
    }

}