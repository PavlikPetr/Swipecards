package com.topface.topface.ui.settings.payment_ninja.view_models

import com.topface.topface.R
import com.topface.topface.ui.settings.payment_ninja.CardInfo
import com.topface.topface.utils.extensions.getCardName
import com.topface.topface.utils.extensions.getString
import kotlin.properties.Delegates

/**
 * Вью модель элемента "карта"
 * Created by ppavlik on 13.03.17.
 */
class CardViewModel(private val cardInfo: CardInfo, val onClickListener: () -> Unit) {
    private val mViewModel by lazy {
        PaymentNinjaPurchasesItemTitleOnlyViewModel(onClickListener)
    }
    var isCardAvailble by Delegates.observable(true) { prop, old, new ->
        with(mViewModel) {
            if (new) {
                icon.set(getCardIcon())
                title.set(getCardTitle())
            } else {
                icon.set(R.drawable.ic_warning)
                title.set(R.string.ninja_no_card_title.getString())
            }
        }
    }

    init {
        isCardAvailble = true
    }

    private fun getCardTitle() = cardInfo.getCardName()

    private fun getCardIcon() =
            R.drawable.ic_warning

    fun getViewModel() = mViewModel
}