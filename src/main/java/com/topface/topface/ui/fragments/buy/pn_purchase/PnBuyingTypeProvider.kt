package com.topface.topface.ui.fragments.buy.pn_purchase

import com.topface.topface.data.BuyButtonData
import com.topface.topface.ui.new_adapter.enhanced.ITypeProvider

/**
 * Type provider for payment ninja list data
 * Created by ppavlik on 02.03.17.
 */
class PnBuyingTypeProvider : ITypeProvider {
    override fun getType(java: Class<*>) = when (java) {
        BuyButtonData::class.java -> 1
        else -> 0
    }
}