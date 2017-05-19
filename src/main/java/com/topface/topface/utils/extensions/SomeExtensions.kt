package com.topface.topface.utils.extensions

import android.app.Activity
import android.os.Parcel
import android.text.TextUtils
import android.view.View

import com.topface.topface.ui.settings.payment_ninja.CardInfo
import com.topface.topface.ui.settings.payment_ninja.PaymentInfo

/**
 * Помойка расширений
 * Created by tiberal on 18.11.16.
 */

fun CharSequence?.goneIfEmpty() = if (TextUtils.isEmpty(this)) View.GONE else View.VISIBLE

fun Activity?.finishWithResult(resultCode: Int) = this?.let {
    setResult(resultCode)
    finish()
}


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
fun CardInfo?.isAvailable() =
        this?.lastFour?.isNotEmpty() ?: false

/**
 * Проверка карты
 */
fun PaymentInfo.isCradAvailable() =
        CardInfo(lastFour = this.lastDigits, type = this.type).isAvailable()

fun Parcel.writeBoolean(bool: Boolean) = writeByte((if (bool) 1 else 0).toByte())

fun Parcel.readBoolean() = readByte().toInt() == 1
