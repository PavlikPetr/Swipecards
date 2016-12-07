package com.topface.topface.ui.fragments.feed.dialogs.dialogs_redesign.dialog_adapter_components

import android.support.v7.widget.LinearLayoutManager
import com.topface.topface.R
import com.topface.topface.databinding.AppDayListBinding
import com.topface.topface.ui.fragments.feed.app_day.AppDayAdapter
import com.topface.topface.ui.fragments.feed.app_day.AppDayImage
import com.topface.topface.ui.fragments.feed.dialogs.dialogs_redesign.AppDayStubItem
import com.topface.topface.ui.fragments.feed.dialogs.dialogs_redesign.AppDayViewModel
import com.topface.topface.ui.fragments.feed.feed_api.FeedApi
import com.topface.topface.ui.new_adapter.enhanced.AdapterComponent
import java.util.*

/**
 * Компоненет итема приложения дня
 * Created by siberia87 on 06.12.16.
 */
class AppDayItemComponent(var mApi: FeedApi) : AdapterComponent<AppDayListBinding, AppDayStubItem>() {
    override val itemLayout: Int
        get() = R.layout.app_day_list
    override val bindingClass: Class<AppDayListBinding>
        get() = AppDayListBinding::class.java
    private val mAdapter by lazy {
        AppDayAdapter()
    }

    override fun bind(binding: AppDayListBinding, data: AppDayStubItem?, position: Int) {
        with(binding.bannerList) {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            layoutManager.isAutoMeasureEnabled = true
            adapter = mAdapter
            setNestedScrollingEnabled(false)
        }
        binding.viewModelRedesign = AppDayViewModel(mApi, {
            it.list?.let {
                mAdapter.addData(it as ArrayList<AppDayImage>)
            }
        })
    }

}