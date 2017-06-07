package com.topface.topface.ui.fragments.buy.design.v1

import android.databinding.ObservableBoolean
import android.databinding.ObservableField
import android.databinding.ObservableInt
import com.topface.framework.utils.Debug
import com.topface.topface.R

class CoinItemViewModel {
    /**
     * иконка на итеме покупки монет, может принимать такие значения
     * ic_purchase_coins_1
     * ic_purchase_coins_2
     * ic_purchase_coins_3
     * ic_purchase_coins_4
     * Иконка с монтеками (число нарисованных монет) имеет 4 состяним:
     * 2 монеты, 3 монеты, кучка и две, кучка и три.
     * Иконка присваивается продукту в зависимости от числа монет,
     * которые он продает в порядке возрастания. Если с сервера пришло более 4 продуктов,
     * то у 4 и далее (по числу монет) отображается 4-е максимальное состояние монет
     */
    val coinsDrawable = ObservableInt(R.drawable.ic_purchase_coins_2)
    //TODO НИЖЕ ГОВНО ПОПРАВЬ ПАРЯ
    // тестовая инициализация полей
    val titleText = ObservableField<String>("13 monet")
    val priceText = ObservableField<String>("za 99 rub")
    /**
     * Бейджик популярное рисуется у тех продуктов, у которых пришел флаг SpecialPrice
     */
    val isSpecial = ObservableBoolean(Math.random() > 0.5)

    fun buy() {
        Debug.log("--- buy coins")
    }
}