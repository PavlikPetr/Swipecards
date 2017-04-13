package com.topface.topface.ui.views.toolbar.utils

/**
 * Менеджер для работы с тулбаром, в него сетятся настройки, а он раcсылает их подписчикам
 * Created by ppavlik on 09.11.16.
 */
object ToolbarManager {
    private var mSettingsListenersList = mutableListOf<IToolbarSettings>()

    fun registerSettingsListener(listener: IToolbarSettings) = mSettingsListenersList.add(listener)

    fun unregisterSettingsListener(listener: IToolbarSettings) = mSettingsListenersList.remove(listener)

    fun setToolbarSettings(settings: ToolbarSettingsData) = mSettingsListenersList.forEach { it.onToolbarSettings(settings) }
}