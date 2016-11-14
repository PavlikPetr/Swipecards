package com.topface.topface.ui.fragments.dating

/**
 * Интерфейс для взаимодействия с вьюхой кнопок в дейтинге
 * Created by tiberal on 13.10.16.
 */
interface IDatingButtonsView {
    fun showControls()
    fun hideControls()
    fun lockControls()
    fun unlockControls()
}