package com.topface.topface.ui.fragments.feed.enhanced.tabbed_likes.stubs

import android.databinding.ObservableField
import com.topface.topface.ui.fragments.feed.enhanced.base.BaseLockScreenViewModel
import com.topface.topface.ui.fragments.feed.feed_base.IFeedUnlocked
import com.topface.topface.utils.Utils

/**
 *  Базовая заглушка дял симпатий-восхущений-взаимных
 */
open class BaseSympathyStubViewModel(mIFeedUnlocked: IFeedUnlocked) : BaseLockScreenViewModel(mIFeedUnlocked) {

    /**
     * текст заголовка заглушки
     */
    val stubTitleText = ObservableField(Utils.EMPTY)

    /**
     * текст самой заглушки
     */
    val stubText = ObservableField(Utils.EMPTY)

    /**
     * текст на зеленой кнопке
     */
    val greenButtonText = ObservableField(Utils.EMPTY)

    /**
     * текст на кнопке без обводки
     */
    val borderlessButtonText = ObservableField(Utils.EMPTY)

    /**
     * действия по нажатию на зеленую кнопку
     */
    var greenButtonAction: () -> Unit = {}

    /**
     * действия по нажатию на кнопку без обводки
     */
    var onBorderlessButtonPress: () -> Unit = {}
}