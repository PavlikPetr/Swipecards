package com.topface.topface.ui.fragments.feed.app_day

import android.os.Bundle
import com.topface.billing.InstantPurchaseModel
import com.topface.topface.R
import com.topface.topface.databinding.ItemAppDayBinding
import com.topface.topface.ui.adapters.BaseRecyclerViewAdapter
import com.topface.topface.utils.extensions.appContext

/**
 * Адаптер для итемов рекламы апы дня
 * Created by siberia87 on 06.10.16.
 */
class AppDayAdapter(private val mInstantPurchaseModel: InstantPurchaseModel) : BaseRecyclerViewAdapter<ItemAppDayBinding, AppDayImage>() {

    override fun bindData(binding: ItemAppDayBinding?, position: Int) = binding?.let { bind ->
        getDataItem(position)?.let {
            bind.viewModel = AppDayItemViewModel(bind.appContext(), it, mInstantPurchaseModel)
        }
    } ?: Unit

    override fun getItemLayout() = R.layout.item_app_day

    override fun getUpdaterEmitObject(): Bundle? = null
}