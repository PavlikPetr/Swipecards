package com.topface.topface.ui.fragments.feed.app_day

import android.databinding.ObservableInt
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.OnScrollListener
import android.view.View
import android.view.ViewTreeObserver
import android.widget.Toast
import com.topface.framework.utils.Debug
import com.topface.topface.databinding.AppDayListBinding
import com.topface.topface.statistics.AppBannerStatistics
import com.topface.topface.ui.fragments.feed.app_day.AppDayImage
import com.topface.topface.utils.extensions.toList
import com.topface.topface.viewModels.BaseViewModel
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.linearLayout
import java.io.LineNumberReader
import java.util.*

/**
 * VM для ленты рекламы приложений дня
 * Created by siberia87 on 06.10.16.
 */
class AppDayViewModel(binding: AppDayListBinding, private val array: List<AppDayImage>) :
        BaseViewModel<AppDayListBinding>(binding) {
    val TAG_LOG = AppDayViewModel::class.java.name

    val isProgressBarVisible = ObservableInt(View.INVISIBLE)
    private var res = mutableListOf<Int>()

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
            binding.bannerList.layoutManager.isAutoMeasureEnabled = true
            adapter = mAdapter
            setNestedScrollingEnabled(false)

            binding.bannerList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    val adapter = recyclerView?.adapter as AppDayAdapter
                    val lm = recyclerView?.layoutManager as LinearLayoutManager
                    val resultSequence = mutableListOf<Int>()

                    val firstCompletelyVisibleItem = lm.findFirstCompletelyVisibleItemPosition()
                    val lastCompletelyVisibleItem = lm.findLastCompletelyVisibleItemPosition()
                    val temp = (firstCompletelyVisibleItem..lastCompletelyVisibleItem).toList()

                    resultSequence.addAll(temp)
                    resultSequence.removeAll(res.intersect(temp))

                    for (i in resultSequence) adapter.getDataItem(i)?.let {
                        AppBannerStatistics.sendBannerShown(it.id)
                        Debug.log(TAG_LOG, resultSequence.toString())
                    }

                    res = temp
                }
            })

        }
    }

}
