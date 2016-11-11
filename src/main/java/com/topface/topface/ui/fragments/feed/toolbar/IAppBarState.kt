package com.topface.topface.ui.fragments.feed.toolbar

/**
 * Интерфейс для отслеживания состояния scrim при работе с AppBar
 * Created by ppavlik on 09.11.16.
 */
interface IAppBarState {
    // true когда начинается переход в scrim
    fun isScrimVisible(isVisible: Boolean)

    // true когда закончилось сворачивание CollapsingToolbar
    fun isCollapsed(isCollapsed: Boolean) {
    }
}