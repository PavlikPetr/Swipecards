package com.topface.topface.ui.views.toolbar

import android.databinding.ObservableField
import android.databinding.ObservableInt
import com.topface.topface.databinding.ToolbarBinding
import com.topface.topface.utils.extensions.getString
import com.topface.topface.viewModels.BaseViewModel
import com.topface.topface.R

/**
 * Created by ppavlik on 14.10.16.
 * Базовая VM для тулбара
 */

abstract class BaseToolbarViewModel(binding: ToolbarBinding,
                                    mNavigation: IToolbarNavigation) : BaseViewModel<ToolbarBinding>(binding) {
    val customView = ObservableInt(0)
    val title = ObservableField<String>(R.string.app_name.getString())
    val background = ObservableInt(R.color.toolbar_background_white)
    val subTitle = ObservableField<String>("")
    val titleTextColor = ObservableInt(R.color.toolbar_title_color)
    val subTitleTextColor = ObservableInt(R.color.toolbar_subtitle_color)
    val titleDrawableRight = ObservableInt(0)
    val upIcon = ObservableInt(R.drawable.ic_arrow_up)

    init {
        binding.toolbar.setNavigationOnClickListener { mNavigation.onUpButtonClick() }
    }
}