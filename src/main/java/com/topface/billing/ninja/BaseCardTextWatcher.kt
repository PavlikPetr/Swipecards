package com.topface.billing.ninja

import android.text.Editable
import android.text.TextWatcher

/**
 * Базовый наблюдатель за вводимым текстом экрана добавления карт. Также форматирует строку. Красавчик, короч
 */

open class BaseCardTextWatcher : TextWatcher {

    override fun afterTextChanged(s: Editable?) = Unit

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit

    fun isInputCorrect(s: Editable, size: Int, dividerPosition: Int, divider: Char): Boolean {
        var isCorrect = s.length <= size
        for (i in 0..s.length - 1) {
            if (i > 0 && (i + 1) % dividerPosition == 0) {
                isCorrect = isCorrect && (divider == s.get(i))
            } else {
                isCorrect = isCorrect && Character.isDigit(s.get(i))
            }
        }
        return isCorrect
    }

    fun concatString(digits: CharArray, dividerPosition: Int, divider: Char): String {
        val formatted = StringBuilder()
        for (i in digits.indices) {
            if (digits[i].toInt() != 0) {
                formatted.append(digits[i])
                if (i > 0 && i < (digits.size - 1) && (((i + 1) % dividerPosition) == 0)) {
                    formatted.append(divider)
                }
            }
        }
        return formatted.toString()
    }

    fun getDigitArray(s: Editable, size: Int): CharArray {
        val digits = CharArray(size)
        var index = 0
        var i = 0
        while (i < s.length && index < size) {
            val current = s[i]
            if (Character.isDigit(current)) {
                digits[index] = current
                index++
            }
            i++
        }
        return digits
    }
}