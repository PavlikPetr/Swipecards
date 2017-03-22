package com.topface.topface.utils.extensions

import com.topface.topface.App
import com.topface.topface.data.BuyButtonData
import com.topface.topface.data.Products
import com.topface.topface.ui.fragments.buy.pn_purchase.PaymentNinjaProduct
import com.topface.topface.ui.fragments.buy.pn_purchase.PaymentNinjaProductsList
import com.topface.topface.utils.CacheProfile
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
                        .getFormattedPrice((price / divider).toDouble()))

fun NumberFormat.getFormattedPrice(price: Double): String =
        with(this) {
            maximumFractionDigits = if (price % 1 != 0.0) 2 else 0
            format(price)
        }

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