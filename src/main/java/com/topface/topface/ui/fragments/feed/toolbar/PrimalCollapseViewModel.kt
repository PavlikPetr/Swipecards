package com.topface.topface.ui.fragments.feed.toolbar

import android.databinding.ObservableField
import android.databinding.ObservableInt
import android.graphics.Color
import android.support.design.widget.AppBarLayout
import android.view.View
import com.topface.topface.databinding.AppBarBinding
import com.topface.topface.viewModels.BaseViewModel
import com.topface.topface.R
import com.topface.topface.utils.extensions.getColor
import com.topface.topface.utils.extensions.isHasNotification

/**
 * Model for interaction with collapsing toolbar
 * Created by tiberal on 18.10.16.
 */
class PrimalCollapseViewModel(binding: AppBarBinding) : BaseViewModel<AppBarBinding>(binding)
        , AppBarLayout.OnOffsetChangedListener {

    val anchorVisibility = ObservableInt(View.VISIBLE)
    val collapseVisibility = ObservableInt(View.VISIBLE)
    val title = ObservableField<String>()

    override fun onOffsetChanged(appBar: AppBarLayout?, verticalOffset: Int) {
        appBar?.let {
            val isScrimsAreShown = it.getHeight() + verticalOffset < binding.collapsingLayout.scrimVisibleHeightTrigger
            binding.collapsingLayout.setCollapsedTitleTextColor((if (isScrimsAreShown) R.color.toolbar_light_title_color else R.color.toolbar_transparent_title_color).getColor())
            with(binding.viewModel) {
                background.set(if (isScrimsAreShown) 0 else R.drawable.tool_bar_gradient)
                titleTextColor.set((if (isScrimsAreShown) R.color.toolbar_dark_title_color else R.color.toolbar_light_title_color).getColor())
                with(upIcon) {
                    if (get().isHasNotification())
                        set(if (isScrimsAreShown) com.topface.topface.R.drawable.menu_gray_notification else com.topface.topface.R.drawable.menu_white_notification)
                    else
                        set(if (isScrimsAreShown) com.topface.topface.R.drawable.menu_gray else com.topface.topface.R.drawable.menu_white)
                }
            }
        }
    }
}