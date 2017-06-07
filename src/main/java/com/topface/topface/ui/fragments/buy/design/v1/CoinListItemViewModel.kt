package com.topface.topface.ui.fragments.buy.design.v1

import com.topface.topface.utils.databinding.MultiObservableArrayList

class CoinListItemViewModel {
    val data: MultiObservableArrayList<Any> by lazy {
        MultiObservableArrayList<Any>()
    }

    init {
        //TODO НИЖЕ ГОВНО ПОПРАВЬ ПАРЯ
        // тестовая инициализация списка монет
        data.add(CoinItem())
        data.add(CoinItem())
        data.add(CoinItem())
        data.add(CoinItem())
    }
}