package com.topface.topface.ui.fragments.feed.people_nearby.people_nerby_redesign

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
 * VM для нового дизайна "Люди рядом"
 * Created by tiberal on 30.11.16.
 */
class PeopleNearbyFragmentViewModel(context: Context, private val mApi: FeedApi,
                                    private val updater: () -> Observable<Bundle>)
    : SwipeRefreshLayout.OnRefreshListener, ILifeCycle, IFeedPushHandlerListener {
    var isRefreshing = ObservableBoolean()
    var isEnable = ObservableBoolean(true)

    override fun onRefresh() {
    }
}