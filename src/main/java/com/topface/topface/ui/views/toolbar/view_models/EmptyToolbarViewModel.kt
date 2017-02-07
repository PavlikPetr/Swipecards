package com.topface.topface.ui.views.toolbar.view_models

import android.view.View
import com.topface.topface.databinding.ToolbarBinding

/**
 * Тулбар как бы есть, но его как бы нет
 * Created by petrp on 03.02.2017.
 */
class EmptyToolbarViewModel(binding: ToolbarBinding) : BaseToolbarViewModel(binding) {
    init {
        visibility.set(View.GONE)
        shadowVisibility.set(View.GONE)
    }
}