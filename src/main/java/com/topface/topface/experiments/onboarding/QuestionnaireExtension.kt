package com.topface.topface.experiments.onboarding

import com.topface.topface.R
import com.topface.topface.experiments.onboarding.question.ValueConditions
import com.topface.topface.utils.Utils
import com.topface.topface.utils.extensions.getString
import com.topface.topface.utils.extensions.safeToInt

/**
 * Различные расширения для опросника
 * Created by petrp on 04.04.2017.
 */

// возвращает Pair<Boolean,String>, где Boolean - найдена ли ошибка при валидации данных,
// а String - текст ошибки
fun Int.getDigitInputError(min: ValueConditions, max: ValueConditions) =
        if (toString().length >= min.value.toString().length) {
            if (this < min.value)
                Pair(true, min.errorMessage)
            else if (this > max.value)
                Pair(true, max.errorMessage)
            else Pair(false, Utils.EMPTY)
        } else Pair(true, Utils.EMPTY)

// возвращает Pair<Boolean,String>, где Boolean - найдена ли ошибка при валидации данных,
// а String - текст ошибки
fun String.getDigitInputError(min: ValueConditions, max: ValueConditions) =
        with(safeToInt(kotlin.Int.MIN_VALUE)) {
            if (this == Int.MIN_VALUE)
                Pair(true, R.string.general_wrong_field_value.getString())
            else getDigitInputError(min, max)
        }

// возвращает Pair<Boolean,String>, где Boolean - найдена ли ошибка при валидации данных,
// а String - текст ошибки
fun String.getTextInputError(min: ValueConditions, max: ValueConditions) =
        if (this == null)
            Pair(true, R.string.general_wrong_field_value.getString())
        else if (this.length < min.value)
            Pair(true, min.errorMessage)
        else if (this.length > max.value)
            Pair(true, max.errorMessage)
        else Pair(false, Utils.EMPTY)