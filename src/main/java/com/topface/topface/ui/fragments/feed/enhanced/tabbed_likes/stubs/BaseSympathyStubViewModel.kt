package com.topface.topface.ui.fragments.feed.enhanced.tabbed_likes.stubs

import android.databinding.ObservableField
import com.topface.topface.ui.fragments.feed.enhanced.base.BaseLockScreenViewModel
import com.topface.topface.ui.fragments.feed.feed_base.IFeedUnlocked

/**
 *  Базовая заглушка дял симпатий-восхущений-взаимных
 */
open class BaseSympathyStubViewModel(mIFeedUnlocked: IFeedUnlocked) : BaseLockScreenViewModel(mIFeedUnlocked) {

    /**
     * текст заголовка заглушки
     */
    val stubTitleText = ObservableField("")

    /**
     * текст самой заглушки
     */
    val stubText = ObservableField("")

    /**
     * текст на зеленой кнопке
     */
    val greenButtonText = ObservableField("")

    /**
     * текст на кнопке без обводки
     */
    val borderlessButtonText = ObservableField("")

    /**
     * действия по нажатию на зеленую кнопку
     */
    var greenButtonAction: () -> Unit = {}

    /**
     * действия по нажатию на кнопку без обводки
     */
    var onBorderlessButtonPress: () -> Unit = {}
}