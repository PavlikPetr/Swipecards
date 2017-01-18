package com.topface.topface.utils.extensions

import com.topface.topface.requests.handlers.ErrorCodes

/**
 * Методы для безопасной конвертации
 * Created by petrp on 18.01.2017.
 */

@JvmOverloads
fun String?.safeToInt(defaulrValue: Int = 0) = this?.let {
    try {
        it.toInt()
    } catch (e: NumberFormatException) {
        defaulrValue
    }
} ?: defaulrValue