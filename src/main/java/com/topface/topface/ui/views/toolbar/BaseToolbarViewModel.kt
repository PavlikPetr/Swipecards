package com.topface.topface.ui.views.toolbar

import android.databinding.ObservableField
import android.databinding.ObservableInt
import android.view.View
import com.topface.topface.R
import com.topface.topface.databinding.ToolbarBinding
import com.topface.topface.utils.extensions.*
import com.topface.topface.viewModels.BaseViewModel

/**
 * Created by ppavlik on 14.10.16.
 * Базовая VM для тулбара
 */

abstract class BaseToolbarViewModel(binding: ToolbarBinding,
                                    val mNavigation: IToolbarNavigation? = null) : BaseViewModel<ToolbarBinding>(binding) {
    val title = ObservableField<String>(R.string.app_name.getString())
    val background = ObservableInt(R.color.toolbar_background_white)
    val subTitle = ObservableField<String>("")
    val titleTextColor = ObservableInt(R.color.toolbar_title_color.getColor())
    val subTitleTextColor = ObservableInt(R.color.toolbar_subtitle_color.getColor())
    val titleDrawableRight = ObservableInt(0)
    val upIcon = ObservableInt(R.drawable.ic_arrow_up_gray)
    val visibility = ObservableInt(View.VISIBLE)
    val shadowVisibility = ObservableInt(View.VISIBLE)

    // увы, но колбэк будет работать только если установить его после setSupportActionBar
    fun init() =
            mNavigation?.let { navigator ->
                binding.toolbar.setNavigationOnClickListener {
                    navigator.onUpButtonClick()
                }
            }

    fun setOnline(isOnline: Boolean) =
            with(titleDrawableRight) {
                set(if (isOnline) R.drawable.im_list_online else 0)
                notifyChange()
            }
}