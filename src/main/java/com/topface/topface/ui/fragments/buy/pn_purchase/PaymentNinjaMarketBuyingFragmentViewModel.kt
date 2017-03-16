package com.topface.topface.ui.fragments.buy.pn_purchase

import android.databinding.ObservableField
import android.databinding.ObservableInt
import android.view.View
import com.topface.topface.App
import com.topface.topface.data.Options
import com.topface.topface.utils.CacheProfile
import com.topface.topface.utils.databinding.SingleObservableArrayList
import com.topface.topface.utils.extensions.getCoinsProducts
import com.topface.topface.utils.extensions.getLikesProducts
import com.topface.topface.utils.extensions.getVipProducts
import com.topface.topface.utils.rx.safeUnsubscribe
import com.topface.topface.utils.rx.shortSubscription
import rx.Subscription

/**
 * Buy buttons view model
 * Created by petrp on 02.03.2017.
 */
class PaymentNinjaMarketBuyingFragmentViewModel(private val mIsVipPurchaseProducts: Boolean) {
    val cardInfoVisibility = ObservableInt(View.GONE)
    val cardInfo = ObservableField("")
    val data = SingleObservableArrayList<Any>().apply {
        if (mIsVipPurchaseProducts) {
            with(CacheProfile.getPaymentNinjaProductsList().getVipProducts().filter { it.displayOnBuyScreen }) {
                if (size > 0) {
                    this@apply.observableList.add(BuyScreenTitle())
                    this@apply.observableList.addAll(this)
                } else {
                    this@apply.observableList.add(BuyScreenProductUnavailable())
                }
            }

        } else {
            with(Pair(CacheProfile.getPaymentNinjaProductsList().getLikesProducts().filter { it.displayOnBuyScreen },
                    CacheProfile.getPaymentNinjaProductsList().getCoinsProducts().filter { it.displayOnBuyScreen })) {
                if (first.isNotEmpty() || second.isNotEmpty()) {
                    this@apply.observableList.add(BuyScreenTitle())
                    if (this@with.first.isNotEmpty()) {
                        this@apply.observableList.add(BuyScreenLikesSection())
                        this@apply.observableList.addAll(this.first)
                    } else Unit
                    if (this@with.second.isNotEmpty()) {
                        this@apply.observableList.add(BuyScreenCoinsSection())
                        this@apply.observableList.addAll(this.second)
                    } else Unit
                } else {
                    this@apply.observableList.add(BuyScreenProductUnavailable())
                }
            }
        }
    }

    private val mOptionsSubscription: Subscription =
            App.getAppComponent().appState().getObservable(Options::class.java).subscribe(shortSubscription {

            })

    fun release() {
        mOptionsSubscription.safeUnsubscribe()
    }
}