package com.topface.topface.ui.fragments.feed.people_nearby.people_nerby_redesign

import android.content.Context
import android.databinding.Observable.OnPropertyChangedCallback
import android.databinding.ObservableBoolean
import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import com.topface.topface.App
import com.topface.topface.state.EventBus
import com.topface.topface.ui.fragments.feed.dialogs.IFeedPushHandlerListener
import com.topface.topface.ui.fragments.feed.feed_api.FeedApi
import com.topface.topface.utils.ILifeCycle
import com.topface.topface.utils.databinding.SingleObservableArrayList
import rx.Observable
import javax.inject.Inject

/**
 * VM для нового дизайна "Люди рядом"
 * Created by tiberal on 30.11.16.
 */
class PeopleNearbyFragmentViewModel(context: Context, private val mApi: FeedApi,
                                    private val updater: () -> Observable<Bundle>)
    : SwipeRefreshLayout.OnRefreshListener, ILifeCycle, IFeedPushHandlerListener {
    var isRefreshing = ObservableBoolean()
    var isEnable = ObservableBoolean(false)
    val data = SingleObservableArrayList<Any>()

    @Inject lateinit var mEventBus: EventBus

    init {
        App.get().inject(this)
        // отслеживаем изменения ptr
        isRefreshing.addOnPropertyChangedCallback(object : OnPropertyChangedCallback() {
            override fun onPropertyChanged(obs: android.databinding.Observable?, p1: Int) {
                // отправляем актуальный статус ptr подписчикам
                (obs as? ObservableBoolean)?.let { mEventBus.setData(PeopleNearbyRefreshStatus(it.get())) }
            }
        })
    }

    override fun onRefresh() {
        isRefreshing.set(true)
    }

    fun release() {

    }
}