package com.topface.topface.utils.extensions

import com.topface.topface.data.BuyButtonData
import com.topface.topface.data.Products
import com.topface.topface.utils.CacheProfile

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
