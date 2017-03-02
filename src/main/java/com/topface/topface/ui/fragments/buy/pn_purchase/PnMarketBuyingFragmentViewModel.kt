package com.topface.topface.ui.fragments.buy.pn_purchase

import android.databinding.ObservableBoolean
import android.databinding.ObservableField
import android.databinding.ObservableInt
import android.view.View
import com.topface.topface.data.BuyButtonData
import com.topface.topface.utils.CacheProfile
import com.topface.topface.utils.databinding.SingleObservableArrayList

/**
 * Buy buttons view model
 * Created by petrp on 02.03.2017.
 */
class PnMarketBuyingFragmentViewModel(private val mIsVipPurchaseProducts: Boolean, private val mTitle: String?) {
    val title = ObservableField(mTitle ?: "")
    val cardInfoVisibility = ObservableInt(View.GONE)
    val cardInfo = ObservableField("")
    val data = SingleObservableArrayList<BuyButtonData>().apply {
        if (mIsVipPurchaseProducts) {
            addAll(CacheProfile.getMarketProducts().premium)
        } else {
            addAll(CacheProfile.getMarketProducts().likes)
            addAll(CacheProfile.getMarketProducts().coins)
        }
    }

    init {

    }
}