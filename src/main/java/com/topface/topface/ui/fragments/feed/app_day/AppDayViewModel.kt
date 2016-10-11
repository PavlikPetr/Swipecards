package com.topface.topface.ui.fragments.feed.app_day

import android.databinding.ObservableInt
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.topface.topface.databinding.AppDayListBinding
import com.topface.topface.ui.fragments.feed.app_day.models.AppDayImage
import com.topface.topface.viewModels.BaseViewModel
import java.util.*

/**
 * VM для ленты рекламы приложений дня
 * Created by siberia87 on 06.10.16.
 */
class AppDayViewModel(binding: AppDayListBinding, private val array: List<AppDayImage>) :
        BaseViewModel<AppDayListBinding>(binding) {

    val isProgressBarVisible = ObservableInt(View.INVISIBLE)
    val mAdapter by lazy {
        val adapter = AppDayAdapter()
        adapter.addData(ArrayList(array))
        adapter
    }

    init {
        with(binding.bannerList) {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            binding.bannerList.layoutManager.isAutoMeasureEnabled = true
            adapter = mAdapter
            setNestedScrollingEnabled(false)
        }
    }
}