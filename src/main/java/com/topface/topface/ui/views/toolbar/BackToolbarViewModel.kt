package com.topface.topface.ui.views.toolbar

import android.support.v7.widget.Toolbar
import com.topface.topface.R
import com.topface.topface.databinding.ToolbarBinding

/**
 * Created by petrp on 09.10.2016.
 * вьюмодель самого простого тулбара со стрелкой влево (upButton) и title
 */

class BackToolbarViewModel @JvmOverloads constructor(binding: ToolbarBinding, titleString: String? = null, mNavigation: IToolbarNavigation? = null)
: BaseToolbarViewModel(binding, mNavigation) {

    init {
        titleString?.let {
            title.set(it)
        }
    }
}
