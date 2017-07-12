package com.topface.topface.ui.fragments.feed.enhanced.tabbed_likes.stubs

/**
 *  Базовая заглушка дял симпатий-восхущений-взаимных
 *
 * @param stubTitle    текст заголовка заглушки
 * @param stubText          текст самой заглушки
 * @param greenButtonText    текст на зеленой кнопке
 * @param borderlessButtonText    текст на кнопке без обводки
 * @param greenButtonAction    действия по нажатию на зеленую кнопку
 * @param onBorderlessButtonPress    действия по нажатию на кнопку без обводки
 */
class BaseSympathyStubViewModel(val stubTitle: String, val stubText: String,
                                val greenButtonText: String, val borderlessButtonText: String,
                                val greenButtonAction: () -> Unit, val onBorderlessButtonPress: () -> Unit)