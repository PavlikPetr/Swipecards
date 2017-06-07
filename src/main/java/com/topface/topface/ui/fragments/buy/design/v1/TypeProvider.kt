package com.topface.topface.ui.fragments.buy.design.v1

import com.topface.topface.ui.new_adapter.enhanced.ITypeProvider

/**
 * общий провайдер типов как для корневого recycler так и для списка монеток
 */
class TypeProvider: ITypeProvider {
    override fun getType(java: Class<*>) = when (java) {
        LikeItem::class.java -> 1
        CoinItem::class.java -> 2
        CoinListItem::class.java -> 3
        else -> 0
    }
}