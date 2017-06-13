package com.topface.topface.utils.extensions

import android.text.TextUtils
import com.topface.topface.App
import com.topface.topface.data.BuyButtonData
import com.topface.topface.data.Products
import com.topface.topface.data.ProductsDetails
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

fun Products.getLikesProducts() = linkedSetOf<BuyButtonData>().apply { addAll(likes) }

fun Products.getCoinsProducts() = linkedSetOf<BuyButtonData>().apply { addAll(coins) }

fun String.isSubscription() = getProduct()?.type?.isSubscription

fun BuyButtonData.getFormatedPrice(): String {
    val productsDetails = CacheProfile.getMarketProductsDetails()
    var currency: Currency
    var currencyFormatter: NumberFormat
    currency = Currency.getInstance(Products.USD)
    currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US)
    currencyFormatter.currency = currency
    var price = price.toDouble() / 100
    if (productsDetails != null && !TextUtils.isEmpty(totalTemplate)) {
        productsDetails.getProductDetail(id)?.let { detail ->
            if (detail.currency != null) {
                price = detail.price / ProductsDetails.MICRO_AMOUNT
                currency = Currency.getInstance(detail.currency)
                currencyFormatter = if (detail.currency.equals(Products.USD, ignoreCase = true))
                    NumberFormat.getCurrencyInstance(Locale.US)
                else
                    NumberFormat.getCurrencyInstance(Locale(App.getLocaleConfig().applicationLocale))
                currencyFormatter.currency = currency
            }
        }
    }
    return currencyFormatter.getFormattedPrice(price)
}

fun PaymentNinjaProductsList.getVipProducts() = this.products.filter { it.type == Constants.PREMIUM }

fun PaymentNinjaProductsList.getLikesProducts() = this.products.filter { it.type == Constants.LIKES }

fun PaymentNinjaProductsList.getCoinsProducts() = this.products.filter {
    it.type == Constants.COINS ||
            it.type == Constants.COINS_SUBSCRIPTION ||
            it.type == Constants.COINS_SUBSCRIPTION_MASKED
}

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