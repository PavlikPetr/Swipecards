package com.topface.topface.ui.views.toolbar

import android.support.design.widget.AppBarLayout
import com.topface.topface.R
import com.topface.topface.databinding.ToolbarBinding

/**
 * Model for interaction with collapsing toolbar
 * Created by tiberal on 18.10.16.
 */
class PrimalCollapseViewModel(binding: ToolbarBinding) : BaseToolbarViewModel(binding)
        , AppBarLayout.OnOffsetChangedListener {

//    val background = ObservableField<Int>(R.drawable.tool_bar_gradient)
//    val anchorVisibility = ObservableInt(View.VISIBLE)
//    val collapseVisibility = ObservableInt(View.VISIBLE)

    init {
        child.set(1)
        title.set("")
    }

    //todo если сумма высоты экрана и verticalOffset меньше равна высоте при которой тулбар закрашивается (24+апи)
    //то убирать фон
    override fun onOffsetChanged(p0: AppBarLayout?, verticalOffset: Int) {
        if (Math.abs(verticalOffset) >= 900) {
            background.set(0)
        } else {
            background.set(R.drawable.tool_bar_gradient)
        }
    }
}