package com.topface.billing.ninja.fragments.add_card

import android.text.Editable

class NumberWatcher: BaseCardTextWatcher() {

    val CARD_NUMBER_TOTAL_SYMBOLS = 23
    val CARD_NUMBER_TOTAL_DIGITS = 19
    val CARD_NUMBER_DIVIDER_MODULO = 5
    val CARD_NUMBER_DIVIDER_POSITION = CARD_NUMBER_DIVIDER_MODULO - 1
    val CARD_NUMBER_DIVIDER = ' '

    override fun afterTextChanged(s: Editable?) {
        s?.let {
            if (!isInputCorrect(s, CARD_NUMBER_TOTAL_SYMBOLS, CARD_NUMBER_DIVIDER_MODULO, CARD_NUMBER_DIVIDER)) {
                s.replace(0, s.length, concatString(getDigitArray(s, CARD_NUMBER_TOTAL_DIGITS), CARD_NUMBER_DIVIDER_POSITION, CARD_NUMBER_DIVIDER))
            }
        }
    }

}