package com.topface.topface.utils.extensions

/**
 * Методы для безопасной конвертации
 * Created by petrp on 18.01.2017.
 */

@JvmOverloads
fun String?.safeToInt(defaultValue: Int = 0) = this?.let {
    try {
        it.toInt()
    } catch (e: NumberFormatException) {
        defaultValue
    }
} ?: defaultValue