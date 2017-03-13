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
 * Вью-модель для работы с элементом списка, который имеет только title
 * Created by petrp on 12.03.2017.
 */

class PaymentNinjaPurchasesItemTitleOnlyViewModel(val onClickListener: () -> Unit) {
    val title = ObservableField(Utils.EMPTY)
    val icon = ObservableInt()
}
