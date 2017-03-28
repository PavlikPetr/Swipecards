package com.topface.billing.ninja

import android.text.Editable

class TrhuWatcher : BaseCardTextWatcher() {

    companion object {
        const val CARD_DATE_TOTAL_SYMBOLS = 5
        const val CARD_DATE_TOTAL_DIGITS = 4
        const val CARD_DATE_DIVIDER_MODULO = 3
        const val CARD_DATE_DIVIDER_POSITION = CARD_DATE_DIVIDER_MODULO - 1
        const val CARD_DATE_DIVIDER = '/'
    }

    override fun afterTextChanged(s: Editable?) {
        s?.let {
            if (!isInputCorrect(s, CARD_DATE_TOTAL_SYMBOLS, CARD_DATE_DIVIDER_MODULO, CARD_DATE_DIVIDER)) {
                s.replace(0, s.length, concatString(getDigitArray(s, CARD_DATE_TOTAL_DIGITS), CARD_DATE_DIVIDER_POSITION, CARD_DATE_DIVIDER));
            }
        }
    }
}