package com.topface.topface.utils.extensions

import com.topface.topface.App
import com.topface.topface.ui.settings.payment_ninja.CardInfo

/**
 * Экстеншин для различных методов по работе с кредитной картой
 * Created by petrp on 12.03.2017.
 */

const private val CARD_NAME_TEMPLATE = "%s **%s"

fun CardInfo.getCardName() =
        String.format(App.getCurrentLocale(), CARD_NAME_TEMPLATE, type, lastDigit)