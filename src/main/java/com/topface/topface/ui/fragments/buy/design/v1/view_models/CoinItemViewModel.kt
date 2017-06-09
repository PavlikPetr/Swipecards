package com.topface.topface.ui.fragments.buy.design.v1.view_models

import android.databinding.ObservableBoolean
import android.databinding.ObservableField
import android.databinding.ObservableInt
import com.topface.framework.utils.Debug
import com.topface.topface.R
import com.topface.topface.data.BuyButtonData
import com.topface.topface.ui.fragments.feed.feed_base.IFeedNavigator
import com.topface.topface.utils.extensions.getString

class CoinItemViewModel(private val data: BuyButtonData, private val mFrom: String, private val mNavigator: IFeedNavigator) {
    /**
     * иконка на итеме покупки монет, может принимать такие значения
     * ic_purchase_coins_1
     * ic_purchase_coins_2
     * ic_purchase_coins_3
     * ic_purchase_coins_4
     * Иконка с монетками (число нарисованных монет) имеет 4 состяним:
     * 2 монеты, 3 монеты, кучка и две, кучка и три.
     * Иконка присваивается продукту в зависимости от числа монет,
     * которые он продает в порядке возрастания. Если с сервера пришло более 4 продуктов,
     * то у 4 и далее (по числу монет) отображается 4-е максимальное состояние монет
     */
    val coinsDrawable = ObservableInt(R.drawable.ic_purchase_coins_2)
    val titleText = ObservableField<String>(data.title)
    val priceText = ObservableField<String>(String.format(R.string.product_coast.getString(), data.price))
    /**
     * Бейджик "популярное" рисуется у тех продуктов, у которых пришел флаг SpecialPrice
     */
    val isSpecial = ObservableBoolean(data.showType in 1..2)

    fun buy() {
        mNavigator.showPurchaseProduct(data.id, mFrom)
    }
}