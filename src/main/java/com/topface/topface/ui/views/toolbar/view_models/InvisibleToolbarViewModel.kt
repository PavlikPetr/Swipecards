package com.topface.topface.ui.views.toolbar.view_models

import android.view.View
import com.topface.topface.databinding.TakePhotoDialogBinding
import com.topface.topface.databinding.ToolbarBinding

/**
 * Отключаем тулбар, но в разметке его все же надо указать
 * Created by ppavlik on 09.11.16.
 */
class InvisibleToolbarViewModel(binding: ToolbarBinding) : BaseToolbarViewModel(binding) {
    init {
        visibility.set(View.GONE)
    }
}