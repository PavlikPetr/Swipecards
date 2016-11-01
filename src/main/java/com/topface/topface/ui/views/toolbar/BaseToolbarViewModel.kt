package com.topface.topface.ui.views.toolbar

import android.databinding.ObservableBoolean
import android.databinding.ObservableField
import android.databinding.ObservableInt
import android.graphics.drawable.Drawable
import android.view.View
import com.topface.internal.ViewCompat
import com.topface.topface.databinding.ToolbarBinding
import com.topface.topface.utils.extensions.getString
import com.topface.topface.viewModels.BaseViewModel
import com.topface.topface.R
import com.topface.topface.utils.extensions.getColor
import com.topface.topface.utils.extensions.getDimen
import com.topface.topface.utils.extensions.getDrawable

/**
 * Created by ppavlik on 14.10.16.
 * Базовая VM для тулбара
 */

abstract class BaseToolbarViewModel(binding: ToolbarBinding,
                                    val mNavigation: IToolbarNavigation? = null) : BaseViewModel<ToolbarBinding>(binding) {
    val title = ObservableField<String>(R.string.app_name.getString())
    val background = ObservableInt(R.drawable.toolbar_background)
    val subTitle = ObservableField<String>("")
    val titleTextColor = ObservableInt(R.color.toolbar_title_color.getColor())
    val subTitleTextColor = ObservableInt(R.color.toolbar_subtitle_color.getColor())
    val titleDrawableRight = ObservableInt(0)
    val upIcon = ObservableInt(R.drawable.ic_arrow_up_gray)
    val visibility = ObservableInt(View.VISIBLE)
    val child = ObservableInt(0)

    val anchorVisibility = ObservableInt(View.VISIBLE)
    val collapseVisibility = ObservableInt(View.VISIBLE)

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

    fun actionBarHeight(): Float {
        val styledAttributes = context.getTheme().obtainStyledAttributes(
                intArrayOf(R.attr.actionBarSize))
        val res = styledAttributes.getDimension(0, 0f).toInt() + R.dimen.toolbar_elevation.getDimen()
        styledAttributes.recycle()
        return res
    }
}