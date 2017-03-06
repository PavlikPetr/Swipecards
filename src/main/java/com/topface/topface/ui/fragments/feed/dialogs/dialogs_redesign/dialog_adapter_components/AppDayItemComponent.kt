package com.topface.topface.ui.fragments.feed.dialogs.dialogs_redesign.dialog_adapter_components

import android.graphics.Rect
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import com.topface.topface.R
import com.topface.topface.databinding.AppDayListBinding
import com.topface.topface.ui.fragments.feed.app_day.AppDayAdapter
import com.topface.topface.ui.fragments.feed.app_day.AppDayImage
import com.topface.topface.ui.fragments.feed.dialogs.dialogs_redesign.AppDayStubItem
import com.topface.topface.ui.fragments.feed.dialogs.dialogs_redesign.AppDayViewModel
import com.topface.topface.ui.new_adapter.enhanced.AdapterComponent
import com.topface.topface.utils.extensions.getDimen
import java.util.*

/**
 * Компоненет итема приложения дня
 * Created by siberia87 on 06.12.16.
 */
class AppDayItemComponent : AdapterComponent<AppDayListBinding, AppDayStubItem>() {
    override val itemLayout: Int
        get() = R.layout.app_day_list
    override val bindingClass: Class<AppDayListBinding>
        get() = AppDayListBinding::class.java

    private var pool: RecyclerView.RecycledViewPool? = null

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView?) {
        pool = recyclerView?.recycledViewPool
    }

    override fun bind(binding: AppDayListBinding, data: AppDayStubItem?, position: Int) = data?.appDay?.let {
        with(binding.bannerList) {
            addItemDecoration(object : RecyclerView.ItemDecoration() {
                override fun getItemOffsets(outRect: Rect?, view: View?, parent: RecyclerView?, state: RecyclerView.State?) {
                    view?.let {
                        val padding = R.dimen.appday_item_decorator_paddings.getDimen().toInt()
                        if (layoutManager != null && layoutManager.getPosition(it) % 2 == 0) {
                            it.setPadding(padding, padding, padding, padding)
                            return
                        }
                        it.setPadding(0, padding, 0, padding)
                    }
                }
            })
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = AppDayAdapter().apply {
                addData(it.list as ArrayList<AppDayImage>)
            }
            recycledViewPool = pool
        }
        binding.viewModelRedesign = AppDayViewModel()
    } ?: Unit

    override fun release() {
        pool = null
    }

}