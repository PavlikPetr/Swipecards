package com.topface.billing.ninja.fragments.add_card

import com.topface.topface.R

class CardType(val name: String, val numberMaxLength: Int, val cvvMaxLength: Int, val cardIcon: Int) {

    companion object {
        const val MAX_LENGTH_CARD_NUMBER_LONG = 23
        const val MAX_LENGTH_CARD_NUMBER_DEFAULT = 19
        const val MAX_LENGTH_CARD_NUMBER_SHORT = 17
        const val MAX_LENGTH_CARD_NUMBER_AMEX = 18

        const val CVV_DEFAULT = 3
        const val CVV_AMEX = 4

        val VISA = CardType("visa", MAX_LENGTH_CARD_NUMBER_DEFAULT, CVV_DEFAULT, R.drawable.ic_card_visa)
        val MASTERCARD = CardType("mastercard", MAX_LENGTH_CARD_NUMBER_LONG, CVV_DEFAULT, R.drawable.ic_card_mastercard)
        val AMERICAN_EXPRESS = CardType("american_express", MAX_LENGTH_CARD_NUMBER_AMEX, CVV_AMEX, R.drawable.ic_card_american_express)
        val DINERS = CardType("diners", MAX_LENGTH_CARD_NUMBER_SHORT, CVV_DEFAULT, R.drawable.ic_card_diners_club)
        val DISCOVER = CardType("discover", MAX_LENGTH_CARD_NUMBER_DEFAULT, CVV_DEFAULT, R.drawable.ic_card_discover)
        val JCB = CardType("jcb", MAX_LENGTH_CARD_NUMBER_DEFAULT, CVV_DEFAULT, R.drawable.ic_card_jcb)
        val MIR = CardType("mir", MAX_LENGTH_CARD_NUMBER_LONG, CVV_DEFAULT, R.drawable.ic_card_mir)
        val DEFAULT = CardType("default", MAX_LENGTH_CARD_NUMBER_LONG, CVV_DEFAULT, R.drawable.ic_card_default)

        val NONVALIDCARD = CardType("non_valid",MAX_LENGTH_CARD_NUMBER_LONG, CVV_DEFAULT, 0 )
    }
}