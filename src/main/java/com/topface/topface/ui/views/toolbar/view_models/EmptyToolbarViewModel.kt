package com.topface.topface.ui.views.toolbar.view_models

import android.view.View
import com.topface.topface.databinding.ToolbarViewBinding

/**
 * Тулбар как бы есть, но его как бы нет
 * Created by petrp on 03.02.2017.
 */
class EmptyToolbarViewModel(binding: ToolbarViewBinding) : BaseToolbarViewModel(binding) {
    init {
        visibility.set(View.GONE)
        shadowVisibility.set(View.GONE)
    }
}