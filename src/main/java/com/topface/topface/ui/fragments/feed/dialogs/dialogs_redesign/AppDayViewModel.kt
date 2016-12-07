package com.topface.topface.ui.fragments.feed.dialogs.dialogs_redesign

import android.databinding.ObservableInt
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import com.topface.framework.utils.Debug
import com.topface.topface.databinding.AppDayListBinding
import com.topface.topface.statistics.AppBannerStatistics
import com.topface.topface.ui.fragments.feed.app_day.AppDay
import com.topface.topface.ui.fragments.feed.app_day.AppDayAdapter
import com.topface.topface.ui.fragments.feed.app_day.AppDayImage
import com.topface.topface.ui.fragments.feed.feed_api.FeedApi
import com.topface.topface.viewModels.BaseViewModel
import org.jetbrains.anko.doAsync
import rx.Subscriber
import java.util.*

/**
 * VM для ленты рекламы приложений дня
 * Created by siberia87 on 06.10.16.
 */
class AppDayViewModel(binding: AppDayListBinding, var mApi: FeedApi) :
        BaseViewModel<AppDayListBinding>(binding) {

    companion object {
        const val TYPE_FEED_FRAGMENT = "dialog"
    }

    var isProgressBarVisible = ObservableInt(View.VISIBLE)
    private var mRes = mutableListOf<Int>()
    private val TAG_LOG = "app_of_the_day_banner_show"
    private val mAdapter by lazy {
        AppDayAdapter()
    }

    init {
        appDayRequest()
        statisticsReviewAdvertising()
    }

    private fun appDayRequest() = mApi.getAppDayRequest(TYPE_FEED_FRAGMENT).subscribe(object : Subscriber<AppDay>() {
        override fun onCompleted() = isProgressBarVisible.set(View.INVISIBLE)
        override fun onError(e: Throwable?) = e?.let { Debug.log("App day banner error request: $it") } ?: Unit
        override fun onNext(appDay: AppDay?) = appDay?.list?.let { it ->
            if (!it.isEmpty()) {
                mAdapter.addData(it as ArrayList<AppDayImage>)
            }
        } ?: Unit
    })

    private fun statisticsReviewAdvertising() = with(binding.bannerList) {
        setHasFixedSize(true)
        layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        layoutManager.isAutoMeasureEnabled = true
        adapter = mAdapter
        setNestedScrollingEnabled(false)

        binding.bannerList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                val adapter = recyclerView?.let { adapter as AppDayAdapter }
                val lm = recyclerView?.let { layoutManager as LinearLayoutManager }
                val resultSequence = mutableListOf<Int>()

                lm?.let {
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
                                    adapter?.getDataItem(it)?.let {
                                        Debug.log(TAG_LOG, "send id: $it")
                                        AppBannerStatistics.sendBannerShown(it.id)
                                    }
                                }
                            }
                }
            }

        })
    }


}

