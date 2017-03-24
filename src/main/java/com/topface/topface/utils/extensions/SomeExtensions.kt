package com.topface.topface.utils.extensions

import com.topface.topface.ui.settings.payment_ninja.CardInfo
import com.topface.topface.ui.settings.payment_ninja.PaymentInfo

/**
 * Помойка расширений
 * Created by tiberal on 18.11.16.
 */

/**
 * Если провал, то -1
 */
fun String?.toIntSafe(): Int {
    try {
        return this?.toInt() ?: -1
    } catch (e: NumberFormatException) {
        return -1
    }
}

/**
 * Если провал, то -1
 */
fun String?.toLongSafe(): Long {
    try {
        return this?.toLong() ?: -1
    } catch (e: NumberFormatException) {
        return -1
    }
}

/**
 * Если провал, то -1
 */
fun String?.toByteSafe(): Byte {
    try {
        return this?.toByte() ?: -1
    } catch (e: NumberFormatException) {
        return -1
    }
}

/**
 * Проверка карты
 */
fun CardInfo.isAvailable() =
        this.lastFour.isNotEmpty() && this.type.isNotEmpty()

/**
 * Проверка карты
 */
fun PaymentInfo.isCradAvailable() =
        CardInfo(lastFour = this.lastDigits, type = this.type).isAvailable()
