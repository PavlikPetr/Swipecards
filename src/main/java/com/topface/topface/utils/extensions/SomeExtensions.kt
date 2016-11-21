package com.topface.topface.utils.extensions

/**
 * Помойка расширений
 * Created by tiberal on 18.11.16.
 */

/**
 * Если провал, то -1
 */
fun String.toIntSafe(): Int {
    try {
        return toInt()
    } catch (e: NumberFormatException) {
        return -1
    }
}

/**
 * Если провал, то -1
 */
fun String.toLongSafe(): Long {
    try {
        return toLong()
    } catch (e: NumberFormatException) {
        return -1
    }
}

/**
 * Если провал, то -1
 */
fun String.toByteSafe(): Byte {
    try {
        return toByte()
    } catch (e: NumberFormatException) {
        return -1
    }
}
