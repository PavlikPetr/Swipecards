package com.topface.topface.ui.fragments.buy.design.v1.view_models

import android.databinding.ObservableField
import com.topface.topface.R
import com.topface.topface.data.BuyButtonData
import com.topface.topface.ui.fragments.feed.feed_base.IFeedNavigator
import com.topface.topface.utils.extensions.getFormatedPrice
import com.topface.topface.utils.extensions.getString

class LikeItemViewModel(private val data: BuyButtonData, private val mFrom: String, private val mNavigator: IFeedNavigator) {
    val titleText = ObservableField(data.title)
    val priceText = ObservableField(String.format(R.string.product_coast.getString(), data.getFormatedPrice()))
    fun buy() {
        mNavigator.showPurchaseProduct(data.id, mFrom)
    }
}