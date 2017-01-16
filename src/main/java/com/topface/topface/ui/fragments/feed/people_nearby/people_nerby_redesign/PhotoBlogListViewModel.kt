package com.topface.topface.ui.fragments.feed.people_nearby.people_nerby_redesign

import android.content.Context
import android.content.Intent
import android.databinding.ObservableBoolean
import android.os.Bundle
import android.support.v7.util.DiffUtil
import com.topface.topface.App
import com.topface.topface.data.FeedListData
import com.topface.topface.data.FeedPhotoBlog
import com.topface.topface.requests.FeedRequest
import com.topface.topface.state.EventBus
import com.topface.topface.ui.fragments.feed.feed_api.FeedApi
import com.topface.topface.ui.fragments.feed.feed_base.BaseFeedFragmentViewModel
import com.topface.topface.ui.new_adapter.enhanced.CompositeAdapter
import com.topface.topface.utils.ILifeCycle
import com.topface.topface.utils.databinding.SingleObservableArrayList
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
class PhotoBlogListViewModel(context: Context, private val mApi: FeedApi, private var mFeedPhotoBlog: FeedListData<FeedPhotoBlog>?,
                             private val getAdapter: () -> CompositeAdapter?) : ILifeCycle {
    val data = SingleObservableArrayList<Any>()
    @Inject lateinit var mEventBus: EventBus
    private var mSubscriptions = CompositeSubscription()
    private var isRefreshing = ObservableBoolean()

    companion object {
        // время в секундах для перезапроса фотоленты
        private const val REQUEST_TIMEOUT = 20L
    }

    init {
        App.get().inject(this)
        // интервал для перезапроса фотоленты, первый запрос делаем без задержек
        mSubscriptions.add(Observable.interval(0, REQUEST_TIMEOUT, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(shortSubscription { loadFeeds() }))
        mSubscriptions.add(mEventBus.getObservable(PeopleNearbyRefreshStatus::class.java)
                .subscribe(shortSubscription {
                    if (it?.isRefreshing ?: false) {
                        loadFeeds()
                    }
                }))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //TODO отловить закрытие активити на постановку в ленту с успешным ьам размещением и вызвать обновление списка
    }

    fun loadFeeds() {
        mSubscriptions.add(mApi.callFeedUpdate(false, FeedPhotoBlog::class.java, Bundle().apply {
            putSerializable(BaseFeedFragmentViewModel.SERVICE, FeedRequest.FeedService.PHOTOBLOG)
        }).delay(2, TimeUnit.SECONDS)
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
                        data?.let {
                            mEventBus.setData(PhotoBlogLoaded(it.items.isEmpty()))
                            val newList = arrayListOf<Any>(PhotoBlogAdd()).apply { addAll(it.items) }
                            getAdapter.invoke()?.let { adapter ->
                                val oldList = adapter.data
                                DiffUtil.calculateDiff(object : DiffUtil.Callback() {
                                    override fun getNewListSize() = newList.size
                                    override fun getOldListSize() = oldList.size
                                    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int)
                                            = oldList.get(oldItemPosition) == newList.get(newItemPosition)

                                    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int)
                                            = oldList.get(oldItemPosition) == newList.get(newItemPosition)
                                }).let {
                                    adapter.data = newList
                                    it.dispatchUpdatesTo(adapter)
                                }
                            } ?: with(this@PhotoBlogListViewModel.data.observableList) {
                                // чистим старый список
                                clear()
                                // добавляем итем перехода в активити постановки в лидеры
                                add(PhotoBlogAdd())
                                // добавляем пачку лидеров
                                addAll(it.items)
                            }

                        } ?: mEventBus.setData(PhotoBlogLoaded(true))
                    }

                }))
    }

    fun release() {
        mSubscriptions.safeUnsubscribe()
    }
}