package com.topface.topface.ui.fragments.feed.app_day

import android.databinding.ObservableInt
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import com.topface.framework.utils.Debug
import com.topface.topface.databinding.AppDayListBinding
import com.topface.topface.statistics.AppBannerStatistics
import com.topface.topface.viewModels.BaseViewModel
import org.jetbrains.anko.doAsync
import java.util.*

/**
 * VM для ленты рекламы приложений дня
 * Created by siberia87 on 06.10.16.
 */
class AppDayViewModel(binding: AppDayListBinding, private val array: List<AppDayImage>) :
        BaseViewModel<AppDayListBinding>(binding) {
    val TAG_LOG = "app_of_the_day_banner_show"

    var isProgressBarVisible = ObservableInt(View.VISIBLE)
    private var mRes = mutableListOf<Int>()

    val mAdapter by lazy {
        val adapter = AppDayAdapter()
        adapter.addData(ArrayList(array))
        adapter
    }

    init {
        with(binding.bannerList) {
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
                        with(lm) {
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
}

