package com.topface.topface.ui.views.toolbar.view_models

import com.topface.topface.databinding.ToolbarViewBinding
import com.topface.topface.ui.views.toolbar.IToolbarNavigation

/**
 * Created by petrp on 09.10.2016.
 * вьюмодель самого простого тулбара со стрелкой влево (upButton) и title
 */

class BackToolbarViewModel @JvmOverloads constructor(binding: ToolbarViewBinding, titleString: String? = null, mNavigation: IToolbarNavigation? = null)
    : BaseToolbarViewModel(binding, mNavigation) {

    init {
        titleString?.let {
            title.set(it)
        }
    }
}
