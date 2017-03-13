package com.topface.topface.ui.settings.payment_ninja.view_models

import android.databinding.ObservableField
import android.databinding.ObservableInt
import com.topface.topface.R
import com.topface.topface.ui.settings.payment_ninja.CardInfo
import com.topface.topface.utils.Utils
import com.topface.topface.utils.extensions.getCardName
import com.topface.topface.utils.extensions.getString
import kotlin.properties.Delegates

/**
 * Вью модель элемента "карта"
 * Created by petrp on 12.03.2017.
 */

class CardViewModel() {
    val title = ObservableField(Utils.EMPTY)
    val icon = ObservableInt()
    var isCardAvailble by Delegates.observable(true) { prop, old, new ->
        if (new) {
            icon.set(getCardIcon())
            title.set(getCardTitle())
        } else {
            icon.set(R.drawable.ic_warning)
            title.set(R.string.ninja_no_card_title.getString())
        }
    }

    init {
        isCardAvailble = true
    }

    private fun getCardTitle() =
            CardInfo("", "").getCardName()

    private fun getCardIcon() =
            R.drawable.ic_warning
}
