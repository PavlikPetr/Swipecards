package com.topface.topface.ui.settings.payment_ninja.view_models

import android.databinding.ObservableField
import android.databinding.ObservableInt
import com.topface.topface.utils.Utils
import kotlin.properties.Delegates

/**
 * Вью модель элемента "карта"
 * Created by petrp on 12.03.2017.
 */

class CardViewModel() {
    val title = ObservableField(Utils.EMPTY)
    val icon = ObservableInt()
    var isCardAvailble by Delegates.observable(true) { prop, old, new ->

    }

    init {
        isCardAvailble = true
    }
}
