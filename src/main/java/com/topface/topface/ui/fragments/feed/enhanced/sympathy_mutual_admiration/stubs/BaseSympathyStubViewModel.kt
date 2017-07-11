package com.topface.topface.ui.fragments.feed.enhanced.sympathy_mutual_admiration.stubs

import android.databinding.ObservableField

/**
 *  Базовая заглушка дял симпатий-восхущений-взаимных
 *
 * @param stubTitleText    текст заголовка заглушки
 * @param stubText          текст самой заглушки
 * @param greenButtonText    текст на зеленой кнопке
 * @param borderlessButtonText    текст на кнопке без обводки
 * @param onGreenButtonPress    действия по нажатию на зеленую кнопку
 * @param borderlessButtonPress    действия по нажатию на кнопку без обводки
 */
class BaseSympathyStubViewModel( val stubTitle: String, val stubText: String,
                                 val greenButtonText: String, val borderlessButtonText: String,
                                val greenButtonAction: () -> Unit, val onBorderlessButtonPress: () -> Unit) {

    fun borderlessButtonAction() = onBorderlessButtonPress()

    fun onGreenButtonPress() = greenButtonAction()

}