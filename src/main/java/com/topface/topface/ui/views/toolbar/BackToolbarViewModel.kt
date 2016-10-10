package com.topface.topface.ui.views.toolbar

import android.support.v7.widget.Toolbar
import com.topface.topface.R

/**
 * Created by petrp on 09.10.2016.
 */

class BackToolbarViewModel (toolbar: Toolbar, title: String, mNavigation: IToolbarNavigation)
: ToolbarBaseViewModel(toolbar, title = title, icon = R.drawable.ic_arrow_up, mNavigation = mNavigation) {
}
