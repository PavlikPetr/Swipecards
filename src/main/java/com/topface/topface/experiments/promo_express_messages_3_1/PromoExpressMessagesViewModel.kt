package com.topface.topface.experiments.promo_express_messages_3_1

import android.databinding.ObservableBoolean
import android.databinding.ObservableField
import android.databinding.ObservableInt
import com.topface.topface.R
import com.topface.topface.utils.Utils
import com.topface.topface.utils.extensions.getColor

/**
 * VM for promo express popup customization
 */
class PromoExpressMessagesViewModel {
    val isCustomDesign = ObservableBoolean(false)
    val titleImageUrl = ObservableField(Utils.EMPTY)
    val customBackgroundColor = ObservableInt(R.color.bg_white.getColor()) // default @color/bg_white

    init {
        PromoExpressMessages3_1.getCustomPopupSettings()?.let {
            isCustomDesign.set(true)
            titleImageUrl.set(it.titleImageURL)
            customBackgroundColor.set(it.backgroundColor)
        }
    }
}