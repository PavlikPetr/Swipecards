package com.topface.billing.ninja.CardUtils

import android.text.Editable
import android.text.SpannableStringBuilder
import com.topface.billing.ninja.fragments.add_card.CardType
import com.topface.framework.utils.Debug
import java.util.*

object UtilsForCard {

    const val SPACE_DIVIDER = " "
    const val SLASH_DIVIDER = "/"
    const val TRHU_LENGTH = 5
    const val INPUT_DELAY = 50L
    const val EMAIL_ADDRESS =
            "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
                    "\\@" +
                    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                    "(" +
                    "\\." +
                    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
                    ")+"

    val cardBrands = hashMapOf(
            Regex("^[2]+.*") to CardType.MIR,
            Regex("^[4]+.*") to CardType.VISA,
            Regex("^5[1-5]+.*") to CardType.MASTERCARD,
            Regex("^3[47]+.*") to CardType.AMERICAN_EXPRESS,
            Regex("^30([0-5]|[68][0-9])+.*") to CardType.DINERS,
            Regex("^(6011|65\\d{2})+.*") to CardType.DISCOVER,
            Regex("^35([2-8][0-9])+.*") to CardType.JCB,
            Regex("") to CardType.DEFAULT)

    fun isValidTrhu(trhu: String): Boolean {
        val trhuText = trhu.replace(SLASH_DIVIDER, "")

        val mounthInt = Integer.parseInt(trhuText.substring(0, 2))
        val yearInt = Integer.parseInt(trhuText.substring(2))

        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val millenium = currentYear / 1000 * 1000

        val currentYearValid = if (yearInt + millenium > currentYear) true else (yearInt + millenium == currentYear) && (mounthInt > calendar.get(Calendar.MONTH))

        return isDigits(trhuText) && (mounthInt in 1..12) && currentYearValid
    }

    //    Алгоритм Луна.
    // ... - алгоритм вычисления контрольной цифры номера пластиковой карты в соответствии со стандартом ISO/IEC 7812, епта
    fun luhnsAlgorithm(number: String): Boolean {
        // сумма всех чисел, стоящих справа
        var rightNumbersSum = 0
        // сумма всех левых чисел
        var leftNumbersSum = 0
        var isRightDigit = true

        for (i in number.length - 1 downTo 0) {
            val digit = Integer.parseInt(number.substring(i, i + 1))
            if (isRightDigit) {
                rightNumbersSum += digit
            } else {
                val num = if (digit * 2 < 9) digit * 2 else digit * 2 - 9
                leftNumbersSum += num
            }
            isRightDigit = !isRightDigit
        }
        return (rightNumbersSum + leftNumbersSum) % 10 == 0

    }

    // todo дополнительная проверка "а не текст ли ты часом?"
    fun isDigits(string: String) = string.matches(Regex("\\d+"))

    fun getCardType(type: String): CardType? =
            cardBrands.map { it.value }.find { it.name.toLowerCase() == type.toLowerCase() }
}
