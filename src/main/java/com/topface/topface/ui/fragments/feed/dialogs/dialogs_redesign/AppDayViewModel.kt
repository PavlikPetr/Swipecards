package com.topface.topface.ui.fragments.feed.dialogs.dialogs_redesign

import android.databinding.ObservableInt
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import com.topface.framework.utils.Debug
import com.topface.topface.statistics.AppBannerStatistics
import com.topface.topface.ui.fragments.feed.app_day.AppDay
import com.topface.topface.ui.fragments.feed.app_day.AppDayAdapter
import com.topface.topface.ui.fragments.feed.feed_api.FeedApi
import com.topface.topface.utils.ListUtils
import org.jetbrains.anko.doAsync
import rx.Subscriber
import rx.Subscription

/**
 * VM для ленты рекламы приложений дня
 * Created by siberia87 on 06.10.16.
 */
class AppDayViewModel(var mApi: FeedApi, var block: (AppDay) -> (Unit)) : RecyclerView.OnScrollListener() {

    companion object {
        const val TYPE_FEED_FRAGMENT = "dialog"
        const val TAG_LOG = "app_of_the_day_banner_show"
    }

    var isProgressBarVisible = ObservableInt(View.VISIBLE)
    private var mAppDayRequestSubscription: Subscription? = null
    private var mRes = mutableListOf<Int>()

    init {
        appDayRequest()
    }

    private fun appDayRequest() {
        mAppDayRequestSubscription = mApi.getAppDayRequest(TYPE_FEED_FRAGMENT).subscribe(object : Subscriber<AppDay>() {
            override fun onCompleted() = isProgressBarVisible.set(View.INVISIBLE)
            override fun onError(e: Throwable?) = e?.let { Debug.log("App day banner error request: $it") } ?: Unit
            override fun onNext(appDay: AppDay?) = appDay?.let {
                isProgressBarVisible.set(View.INVISIBLE)
                if (ListUtils.isNotEmpty(it.list)) {
                    block(it)
                }
            } ?: Unit
        })
    }

    override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
        super.onScrollStateChanged(recyclerView, newState)
        recyclerView ?: return
        val resultSequence = mutableListOf<Int>()
        (recyclerView.layoutManager as LinearLayoutManager).let {
            with(it) {
                findFirstCompletelyVisibleItemPosition()..findLastCompletelyVisibleItemPosition()
            }
                    .toMutableList()
                    .apply {
                        with(resultSequence) {
                            addAll(this@apply)
                            removeAll(mRes.intersect(this@apply))
                        }
                    }
                    .apply { mRes = this }
                    .forEach {
                        Debug.log(TAG_LOG, "id: $it")
                        doAsync {
                            (recyclerView.adapter as AppDayAdapter).getDataItem(it)?.let {
                                Debug.log(TAG_LOG, "send id: $it")
                                AppBannerStatistics.sendBannerShown(it.id)
                            }
                        }
                    }
        }
    }

    fun release() = mAppDayRequestSubscription?.unsubscribe()
}

