package com.topface.topface.ui.dialogs.trial_vip_experiment.base

import com.topface.topface.R

/**
 * Билдер метрик для разных попапов триала
 * Created by tiberal on 16.11.16.
 */

class BoilerplateDialogMetrics private constructor(val titleTopMargin: Int, val titleBottomMargin: Int,
                                                   val contentBottomMargin: Int, val getVipBottomMargin: Int,
                                                   val descriptionBottomMargin: Int, val popupBackground: Int,
                                                   val getVipButtonBackground: Int) {

    private constructor(builder: Builder) : this(builder.titleTopMargin,
            builder.titleBottomMargin, builder.contentBottomMargin, builder.getVipBottomMargin,
            builder.descriptionBottomMargin, builder.popupBackground, builder.getVipButtonBackground)

    /**
     * @param titleTopMargin - верхний отступ от титула
     * @param titleBottomMargin - нижний отступ от титула
     * @param contentBottomMargin - нижний отсту от контента(то, что разное)
     * @param getVipBottomMargin  - нижний отступ от кнопки покупки
     * @param descriptionBottomMargin - нижний отступ от описания под кнопкой покупки
     * @param popupBackground - ресурс фона (по умолчанию R.color.bg_white)
     * @param getVipButtonBackground - ресурс фона (по умолчанию R.color.buy_coins_button_bg)
     */
    companion object {
        fun create(init: Builder.() -> Unit) =
                Builder(init).build()
    }

    class Builder private constructor() {

        constructor(init: Builder.() -> Unit) : this() {
            init()
        }

        var titleTopMargin = 0
        var titleBottomMargin = 0
        var contentBottomMargin = 0
        var getVipBottomMargin = 0
        var descriptionBottomMargin = 0
        var popupBackground = R.color.bg_white
        var getVipButtonBackground = R.color.buy_coins_button_bg

        fun build() = BoilerplateDialogMetrics(this)

    }
}