package com.topface.topface.ui.fragments.feed.toolbar

import android.databinding.ObservableInt
import android.support.design.widget.AppBarLayout
import android.view.View
import com.topface.framework.utils.Debug
import com.topface.topface.databinding.AppBarBinding
import com.topface.topface.viewModels.BaseViewModel
import com.topface.topface.R
import com.topface.topface.ui.views.toolbar.view_models.NavigationToolbarViewModel
import com.topface.topface.utils.extensions.isHasNotification

/**
 * Model for interaction with collapsing toolbar
 * Created by tiberal on 18.10.16.
 */
class PrimalCollapseViewModel(binding: AppBarBinding, val mScrimStateListener: IAppBarState) : BaseViewModel<AppBarBinding>(binding)
        , AppBarLayout.OnOffsetChangedListener {

    val anchorVisibility = ObservableInt(View.VISIBLE)
    val collapseVisibility = ObservableInt(View.VISIBLE)

    override fun onOffsetChanged(appBar: AppBarLayout?, verticalOffset: Int) {
        appBar?.let {
            val visiblePartSize = it.getHeight() + verticalOffset
            val isScrimsAreShown = visiblePartSize < binding.collapsingLayout.scrimVisibleHeightTrigger
            val isCollapsed = visiblePartSize <= binding.toolbarInclude.root.height
            mScrimStateListener.isScrimVisible(isScrimsAreShown)
            mScrimStateListener.isCollapsed(isCollapsed)
            (binding.viewModel as? NavigationToolbarViewModel)?.let {
                // делаем title видимым только когда началась анимация по сворачиванию collapsingToolbar
                it.extraViewModel.titleVisibility.set(if (isScrimsAreShown) View.VISIBLE else View.GONE)
                // тулбар с градиентом только до тех пор, пока не началась анимация по переходу между CollapsingToolbar и Toolbar
                it.background.set(if (isScrimsAreShown) 0 else R.drawable.tool_bar_gradient)
                // Для того чтобы на белом фоне тулбара была видна кнопка гамбургер-меню необходимо выполнить
                // замену ресурсов в момент перехода между CollapsingToolbar и Toolbar
                with(it.upIcon) {
                    // проверяем текущий ресурс, это надо чтобы заменить серую иконку с индикатором нотификаций
                    // на такую же, но белую и наоборот
                    if (get().isHasNotification())
                        set(if (isScrimsAreShown)
                            com.topface.topface.R.drawable.menu_gray_notification
                        else
                            com.topface.topface.R.drawable.menu_white_notification)
                    else
                        set(if (isScrimsAreShown)
                            com.topface.topface.R.drawable.menu_gray
                        else
                            com.topface.topface.R.drawable.menu_white)
                }
            }
        }
    }
}