package com.topface.topface.utils.extensions

import com.google.android.gms.analytics.ecommerce.Product
import com.topface.topface.App
import com.topface.topface.data.BuyButtonData
import com.topface.topface.data.Products
import com.topface.topface.ui.fragments.buy.pn_purchase.PaymentNinjaProduct
import com.topface.topface.ui.fragments.buy.pn_purchase.PaymentNinjaProduct.Companion.EMPTY_FLOAT
import com.topface.topface.ui.fragments.buy.pn_purchase.PaymentNinjaProduct.Companion.EMPTY_INT
import com.topface.topface.ui.fragments.buy.pn_purchase.PaymentNinjaProductsList
import com.topface.topface.ui.fragments.buy.pn_purchase.PaymentNinjaSubscriptionInfo
import com.topface.topface.utils.CacheProfile
import com.topface.topface.utils.Utils
import java.text.NumberFormat
import java.util.*

fun String.getProduct() = CacheProfile.getMarketProducts()
        .getAllProductButtons()
        .find { it.id == this }

fun Products.getAllProductButtons() = linkedSetOf<BuyButtonData>().apply {
    with(this) {
        addAll(likes)
        addAll(coins)
        addAll(premium)
        addAll(others)
        addAll(coinsSubscriptions)
        addAll(coinsSubscriptionsMasked)
    }
}

fun String.isSubscription() = getProduct()?.type?.isSubscription

fun PaymentNinjaProductsList.getVipProducts() = this.products.filter { it.type == Constants.PREMIUM }

fun PaymentNinjaProductsList.getLikesProducts() = this.products.filter { it.type == Constants.LIKES }

fun PaymentNinjaProductsList.getCoinsProducts() = this.products.filter {
    it.type == Constants.COINS ||
            it.type == Constants.COINS_SUBSCRIPTION ||
            it.type == Constants.COINS_SUBSCRIPTION_MASKED
}

fun PaymentNinjaProduct.getTitle() =
        titleTemplate.replace(Products.PRICE_PER_ITEM,
                NumberFormat.getCurrencyInstance(Locale(App.getLocaleConfig().applicationLocale))
                        .getFormatedPrice((price / divider).toDouble()))

fun NumberFormat.getFormatedPrice(price: Double) =
        with(this) {
            setMaximumFractionDigits(if (price % 1 != 0.0) 2 else 0)
            format(price)
        }

fun PaymentNinjaProduct.isEmpty() =
        id == Utils.EMPTY && showType == EMPTY_INT && titleTemplate == Utils.EMPTY && totalPriceTemplate == Utils.EMPTY &&
                !isSubscription && period == EMPTY_INT && price == EMPTY_INT && type == Utils.EMPTY && value == EMPTY_INT &&
                trialPeriod == EMPTY_INT && !displayOnBuyScreen && durationTitle == Utils.EMPTY && divider == EMPTY_FLOAT &&
                typeOfSubscription == EMPTY_INT && infoOfSubscription.isEmpty()

fun PaymentNinjaSubscriptionInfo.isEmpty() =
        text == Utils.EMPTY && url == Utils.EMPTY

object Constants {
    const val PREMIUM = "premium"
    const val COINS = "coins"
    const val ENERGY = "energy"
    const val LEADER = "leader"
    const val LIKES = "likes"
    const val COINS_SUBSCRIPTION = "coinsSubscription"
    const val COINS_SUBSCRIPTION_MASKED = "coinsSubscriptionMasked"
    const val OTHERS = "others"
}