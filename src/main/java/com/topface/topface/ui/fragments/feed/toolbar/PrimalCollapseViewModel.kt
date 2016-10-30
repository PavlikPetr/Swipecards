package com.topface.topface.ui.fragments.feed.toolbar

import android.databinding.ObservableField
import android.databinding.ObservableInt
import android.support.design.widget.AppBarLayout
import android.view.View
import com.topface.topface.R
import com.topface.topface.databinding.AppBarBinding
import com.topface.topface.viewModels.BaseViewModel

/**
 * Model for interaction with collapsing toolbar
 * Created by tiberal on 18.10.16.
 */
class PrimalCollapseViewModel(binding: AppBarBinding) : BaseViewModel<AppBarBinding>(binding)
        , AppBarLayout.OnOffsetChangedListener {

    val background = ObservableField<Int>(R.drawable.tool_bar_gradient)
    val anchorVisibility = ObservableInt(View.VISIBLE)
    val collapseVisibility = ObservableInt(View.VISIBLE)

    //todo если сумма высоты экрана и verticalOffset меньше равна высоте при которой тулбар закрашивается (24+апи)
    //то убирать фон
    override fun onOffsetChanged(p0: AppBarLayout?, verticalOffset: Int) {
        if (Math.abs(verticalOffset) >= 900) {
            background.set(0)
        } else {
            background.set(R.drawable.tool_bar_gradient)
        }
    }

    override fun release() {
        super.release()
        background.set(0)
    }

}