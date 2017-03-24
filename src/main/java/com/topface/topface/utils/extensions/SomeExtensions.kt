package com.topface.topface.utils.extensions

import android.os.Parcel

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

fun Parcel.writeBoolean(bool: Boolean) = writeByte((if (bool) 1 else 0).toByte())

fun Parcel.readBoolean() = readByte().toInt() == 1
