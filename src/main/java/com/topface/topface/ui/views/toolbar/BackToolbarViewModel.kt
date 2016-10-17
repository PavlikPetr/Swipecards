package com.topface.topface.ui.views.toolbar

import android.support.v7.widget.Toolbar
import com.topface.topface.R
import com.topface.topface.databinding.ToolbarBinding

/**
 * Created by petrp on 09.10.2016.
 */

class BackToolbarViewModel(binding: ToolbarBinding, titleString: String, mNavigation: IToolbarNavigation)
: BaseToolbarViewModel(binding, mNavigation) {

    init {
        title.set(titleString)
    }
}
