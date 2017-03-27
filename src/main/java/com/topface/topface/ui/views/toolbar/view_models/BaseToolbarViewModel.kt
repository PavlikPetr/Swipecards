package com.topface.topface.ui.views.toolbar.view_models

import android.databinding.ObservableInt
import android.view.View
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.databinding.ToolbarViewBinding
import com.topface.topface.ui.views.toolbar.IToolbarNavigation
import com.topface.topface.utils.Utils.getStatusBarHeight
import com.topface.topface.utils.extensions.appContext
import com.topface.topface.utils.extensions.getColor
import com.topface.topface.utils.extensions.getString
import com.topface.topface.utils.rx.RxFieldObservable
import com.topface.topface.viewModels.BaseViewModel


/**
 * Created by ppavlik on 14.10.16.
 * Базовая VM для тулбара
 */

abstract class BaseToolbarViewModel(binding: ToolbarViewBinding,
                                    val mNavigation: IToolbarNavigation? = null) : BaseViewModel<ToolbarViewBinding>(binding) {
    val title = RxFieldObservable<String>(R.string.app_name.getString())
    val background = ObservableInt(R.color.toolbar_background)
    val subTitle = RxFieldObservable<String>()
    val titleTextColor = ObservableInt(R.color.toolbar_title_color.getColor())
    val subTitleTextColor = ObservableInt(R.color.toolbar_subtitle_color.getColor())
    val upIcon = ObservableInt(R.drawable.ic_arrow_up_gray)
    val visibility = ObservableInt(View.VISIBLE)
    val shadowVisibility = ObservableInt(View.VISIBLE)
    /**
     * Верхний отступ/высота подставляемой вместо statusBar вьюшки
     * для toolBar'ов, с прозрачным statusBar
     */
    val topPadding = ObservableInt(0)

    // увы, но колбэк будет работать только если установить его после setSupportActionBar
    fun init() {
        mNavigation?.let { navigator ->
            binding.toolbar.setNavigationOnClickListener {
                navigator.onUpButtonClick()
            }
        }
    }

    /**
     * Used to update toolbar_view top padding with translucent status bar if need
     */
    protected fun updateTopPadding() {
        // если включен новый дизайн диалогов, то надо добавить паддинг, что бы тулбар
        // был ниже прозрачного статус бара
        // учитывать topPadding надо только в тулбарах с прозрачным StatusBar'ом
        if (App.get().options.datingRedesignEnabled) topPadding.set(getStatusBarHeight(binding.appContext()))
    }
}