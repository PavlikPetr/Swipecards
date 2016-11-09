package com.topface.topface.ui.views.toolbar.view_models

import com.topface.topface.R
import com.topface.topface.databinding.ToolbarBinding
import com.topface.topface.ui.views.toolbar.IToolbarNavigation
import com.topface.topface.utils.extensions.getColor

/**
 * Created by ppavlik on 18.10.16.
 * вьюмодель тулбара для фотоальбома
 */
class PhotoSwitcherToolbarViewModel @JvmOverloads constructor(binding: ToolbarBinding,
                                                              mNavigation: IToolbarNavigation? = null)
: BaseToolbarViewModel(binding, mNavigation) {

    init {
        background.set(R.drawable.photoswitcher_toolbar_background)
        upIcon.set(R.drawable.ic_arrow_up)
        titleTextColor.set(R.color.toolbar_title_white.getColor())
    }
}