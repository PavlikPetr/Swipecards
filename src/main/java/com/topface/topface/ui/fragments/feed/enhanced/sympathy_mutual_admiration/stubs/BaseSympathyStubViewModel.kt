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
 * @param onBorderlessButtonPress    действия по нажатию на кнопку без обводки
 */
class BaseSympathyStubViewModel(stubTitleText: String, stubText: String,
                                greenButtonText: String, borderlessButtonText: String,
                                val greenButtonAction: () -> Unit, val onBorderlessButtonPress: () -> Unit) {

    val stubTitle = ObservableField<String>(stubTitleText)
    val stubText = ObservableField<String>(stubText)
    val greenButtonText = ObservableField<String>(greenButtonText)
    val borderlessButtonText = ObservableField<String>(borderlessButtonText)

    fun borderlessButtonAction() = onBorderlessButtonPress()

    fun onGreenButtonPress() = greenButtonAction()

}