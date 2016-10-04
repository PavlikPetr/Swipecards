package com.topface.topface.utils.extensions

import android.view.View
import com.topface.topface.App
import com.topface.topface.data.BuyButtonData
import com.topface.topface.data.Products

/**
 * Created by ppavlik on 29.09.16.
 * Утилиты и расширения для ui-тестирования
 */

private val BUY_BUTTON_TEMPLATE = "%d_%s"
private val BUY_SUBSCRIPTION_BUTTON_TEMPLATE = "${BUY_BUTTON_TEMPLATE}_on_%d_days"
private val BUY_TRIAL_SUBSCRIPTION_BUTTON_TEMPLATE = "${BUY_SUBSCRIPTION_BUTTON_TEMPLATE}_with_%d_trial_days"

fun BuyButtonData.getTag(): String {
    with(this) {
        return String.format(App.getCurrentLocale(), if (type.isSubscription)
            if (trialPeriodInDays > 0) BUY_TRIAL_SUBSCRIPTION_BUTTON_TEMPLATE
            else BUY_SUBSCRIPTION_BUTTON_TEMPLATE
        else BUY_BUTTON_TEMPLATE,
                amount,
                when (type) {
                    Products.ProductType.COINS, Products.ProductType.COINS_SUBSCRIPTION, Products.ProductType.COINS_SUBSCRIPTION_MASKED -> "coins"
                    Products.ProductType.LEADER -> "leader"
                    Products.ProductType.LIKES -> "likes"
                    Products.ProductType.PREMIUM -> "premium"
                    else -> "other"
                },
                periodInDays, trialPeriodInDays)
    }
}

fun View.setTag(tag: Any): View {
    this.setTag(tag)
    return this
}