package com.topface.topface.ui.fragments.feed.toolbar

import android.databinding.ObservableInt
import android.support.design.widget.AppBarLayout
import android.view.View
import com.topface.topface.R
import com.topface.topface.databinding.AppBarBinding
import com.topface.topface.ui.fragments.feed.toolbar.CustomCoordinatorLayout.ViewConfig
import com.topface.topface.viewModels.BaseViewModel

/**
 * Model for interaction with collapsing toolbar
 * Created by tiberal on 18.10.16.
 */
class PrimalCollapseViewModel(binding: AppBarBinding,
                              private val mScrimStateListener: IAppBarState) : BaseViewModel<AppBarBinding>(binding)
        , AppBarLayout.OnOffsetChangedListener {

    // вводим коррективы для скрола в альбоме, в приоритете горизонтальный скрол
    val viewConfigList = listOf<ViewConfig>(ViewConfig(R.id.dating_album, 1f, 0.4f, true))

    val anchorVisibility = ObservableInt(View.VISIBLE)
    val collapseVisibility = ObservableInt(View.VISIBLE)
    val shadowVisibility = ObservableInt(View.VISIBLE)

    override fun onOffsetChanged(appBar: AppBarLayout?, verticalOffset: Int) {
        appBar?.let {
            val visiblePartSize = it.height + verticalOffset
            val isScrimsAreShown = visiblePartSize < binding.collapsingLayout.scrimVisibleHeightTrigger
            val isCollapsed = visiblePartSize <= binding.toolbarInclude.root.height
            mScrimStateListener.isScrimVisible(isScrimsAreShown)
            mScrimStateListener.isCollapsed(isCollapsed)
            shadowVisibility.set(if (isCollapsed) View.VISIBLE else View.GONE)
        }
    }
}