package com.topface.topface.ui.fragments.feed.people_nearby.people_nerby_redesign

import android.content.Intent
import android.databinding.ObservableBoolean
import android.os.Bundle
import com.topface.topface.App
import com.topface.topface.data.FeedListData
import com.topface.topface.data.FeedPhotoBlog
import com.topface.topface.requests.FeedRequest
import com.topface.topface.state.EventBus
import com.topface.topface.ui.fragments.feed.feed_api.FeedApi
import com.topface.topface.ui.fragments.feed.feed_base.BaseFeedFragmentViewModel
import com.topface.topface.ui.fragments.feed.photoblog.PhotoblogFragment
import com.topface.topface.utils.ILifeCycle
import com.topface.topface.utils.databinding.MultiObservableArrayList
import com.topface.topface.utils.rx.applySchedulers
import com.topface.topface.utils.rx.safeUnsubscribe
import com.topface.topface.utils.rx.shortSubscription
import rx.Observable
import rx.Subscriber
import rx.android.schedulers.AndroidSchedulers
import rx.subscriptions.CompositeSubscription
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * ViewMdel для элемента списка, который содержит фотоленту с горизонтальным скролом
 * Created by ppavlik on 12.01.17.
 */
class PhotoBlogListViewModel(private val mApi: FeedApi) : ILifeCycle {
    val data = MultiObservableArrayList<Any>()
    @Inject lateinit var mEventBus: EventBus
    private var mSubscriptions = CompositeSubscription()
    private var isRefreshing = ObservableBoolean()

    companion object {
        // время в секундах для перезапроса фотоленты
        private const val REQUEST_TIMEOUT = 20L
    }

    init {
        App.get().inject(this)
        mSubscriptions.add(mEventBus.getObservable(PeopleNearbyRefreshStatus::class.java)
                .subscribe(shortSubscription {
                    if (it?.isRefreshing ?: false) {
                        loadFeeds(true)
                    }
                }))
        mSubscriptions.add(Observable.interval(0, REQUEST_TIMEOUT, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(shortSubscription { loadFeeds() }))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PhotoblogFragment.ADD_TO_PHOTO_BLOG_ACTIVITY_ID) {
            with(App.getUserConfig()) {
                peopleNearbyPopoverClose = Long.MAX_VALUE
                saveConfig()
            }
            loadFeeds()
        }
    }

    fun loadFeeds(isPullToRefresh: Boolean = false) {
        mSubscriptions.add(mApi.callFeedUpdate(false, FeedPhotoBlog::class.java, Bundle().apply {
            putSerializable(BaseFeedFragmentViewModel.SERVICE, FeedRequest.FeedService.PHOTOBLOG)
        })
                .applySchedulers()
                .subscribe(object : Subscriber<FeedListData<FeedPhotoBlog>>() {
                    override fun onCompleted() {
                        if (isRefreshing.get()) {
                            isRefreshing.set(false)
                        }
                    }

                    override fun onError(e: Throwable?) {
                        e?.let {
                        }
                        if (isRefreshing.get()) {
                            isRefreshing.set(false)
                        }
                    }

                    override fun onNext(data: FeedListData<FeedPhotoBlog>?) {
                        mEventBus.setData(PhotoBlogLoaded(data?.items?.isEmpty() ?: true, isPullToRefresh))
                        data?.let {
                            with(this@PhotoBlogListViewModel.data) {
                                replaceData(arrayListOf<Any>(PhotoBlogAdd())
                                        .apply { addAll(it.items) })
                            }
                        }
                    }
                }))
    }

    fun release() {
        mSubscriptions.safeUnsubscribe()
    }
}