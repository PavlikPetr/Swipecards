package com.topface.topface.ui.fragments.buy.design.v1

import com.topface.topface.R
import com.topface.topface.data.BuyButtonData

/**
 * итем покупки лайков
 *
 * @param data объект продукта GP
 *
 */
data class LikeItem(val data: BuyButtonData, val from:String)

/**
 * итем покупки монеток
 *
 * @param data объект продукта GP
 */
class CoinItem(val data: BuyButtonData, val from:String, val img: Int = R.drawable.ic_purchase_coins_2)

/**
 * итем переключающий тестовые покупки
 *
 * @param - test purchases switch current state
 */
data class TestPurchaseSwitchItem(var isChecked: Boolean)

// Итем сообщающий о том, что покупку не совершить
data class InAppBillingUnsupported(private val diffTemp: Int = 0)