package com.topface.topface.ui.fragments.feed.dialogs.dialogs_redesign.dialog_adapter_components

import com.topface.topface.R
import com.topface.topface.databinding.AppDayListBinding
import com.topface.topface.ui.fragments.feed.dialogs.dialogs_redesign.AppDayStubItem
import com.topface.topface.ui.fragments.feed.dialogs.dialogs_redesign.AppDayViewModel
import com.topface.topface.ui.fragments.feed.feed_api.FeedApi
import com.topface.topface.ui.new_adapter.enhanced.AdapterComponent

/**
 * Компоненет итема приложения дня
 * Created by siberia87 on 06.12.16.
 */
class AppDayItemComponent(var mApi: FeedApi) : AdapterComponent<AppDayListBinding, AppDayStubItem>() {
    override val itemLayout: Int
        get() = R.layout.app_day_list
    override val bindingClass: Class<AppDayListBinding>
        get() = AppDayListBinding::class.java

    override fun bind(binding: AppDayListBinding, data: AppDayStubItem?, position: Int) {
        binding.viewModelRedesign = AppDayViewModel(binding, mApi)
    }
}