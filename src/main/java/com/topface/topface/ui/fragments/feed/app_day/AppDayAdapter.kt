package com.topface.topface.ui.fragments.feed.app_day

import android.os.Bundle
import com.topface.topface.R
import com.topface.topface.databinding.ItemAppDayBinding
import com.topface.topface.ui.adapters.BaseRecyclerViewAdapter
import com.topface.topface.ui.adapters.ItemEventListener
import com.topface.topface.ui.fragments.feed.app_day.AppDayImage

/**
 * Адаптер для итемов рекламы апы дня
 * Created by siberia87 on 06.10.16.
 */
class AppDayAdapter : BaseRecyclerViewAdapter<ItemAppDayBinding, AppDayImage>() {

    override fun bindData(binding: ItemAppDayBinding?, position: Int) = binding?.let { bind ->
        getDataItem(position)?.let {
            bind.root.tag = null  //хак, чтобы избежать java.lang.IllegalArgumentException: You must not call setTag() on a view Glide is targeting
            bind.viewModel = AppDayItemViewModel(bind, it)
        }

    } ?: Unit

    override fun getItemLayout() = R.layout.item_app_day

    override fun getUpdaterEmitObject(): Bundle? = null
}