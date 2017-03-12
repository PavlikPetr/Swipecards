package com.topface.topface.utils.extensions

import android.view.View
import com.topface.framework.utils.Debug
import com.topface.topface.App
import com.topface.topface.R.id.amount
import com.topface.topface.data.BuyButtonData
import com.topface.topface.data.Gift
import com.topface.topface.data.Products
import com.topface.topface.ui.fragments.buy.pn_purchase.PaymentNinjaProduct

/**
 * Created by ppavlik on 29.09.16.
 * Утилиты и расширения для ui-тестирования
 */

const private val BUY_BUTTON_TEMPLATE = "%d_%s"
const private val BUY_SUBSCRIPTION_BUTTON_TEMPLATE = "${BUY_BUTTON_TEMPLATE}_on_%d_days"
const private val BUY_TRIAL_SUBSCRIPTION_BUTTON_TEMPLATE = "${BUY_SUBSCRIPTION_BUTTON_TEMPLATE}_with_%d_trial_days"
const private val GIFT_TEMPLATE = "gift_%d"

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

fun PaymentNinjaProduct.getTag(): String {
    with(this) {
        return String.format(App.getCurrentLocale(), if (isSubscription)
            if (trialPeriod > 0) BUY_TRIAL_SUBSCRIPTION_BUTTON_TEMPLATE
            else BUY_SUBSCRIPTION_BUTTON_TEMPLATE
        else BUY_BUTTON_TEMPLATE,
                amount,
                when (type) {
                    Constants.COINS, Constants.COINS_SUBSCRIPTION, Constants.COINS_SUBSCRIPTION_MASKED -> "coins"
                    Constants.LEADER -> "leader"
                    Constants.LIKES -> "likes"
                    Constants.PREMIUM -> "premium"
                    else -> "other"
                },
                period, trialPeriod)
    }
}

fun Gift.getGiftTag() = String.format(GIFT_TEMPLATE, id)

/**
 * Set tag for qa/debug build and for editors in release build
 */
fun View.setUiTestTag(tag: Any) = apply {
    if (Debug.isDebugLogsEnabled()) {
        setTag(tag)
    }
}