package com.topface.topface.ui.settings.payment_ninja

/**
 * Интерфейс для показа дмалога
 * Created by petrp on 21.03.2017.
 */
interface IAlertDialog {
    fun show(positive: () -> Unit)
}