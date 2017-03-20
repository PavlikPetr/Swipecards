package com.topface.topface.ui.fragments.buy.pn_purchase

import android.databinding.ObservableBoolean
import android.databinding.ObservableField
import android.databinding.ObservableInt
import android.view.View
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.data.Options
import com.topface.topface.utils.CacheProfile
import com.topface.topface.utils.databinding.SingleObservableArrayList
import com.topface.topface.utils.extensions.getCoinsProducts
import com.topface.topface.utils.extensions.getLikesProducts
import com.topface.topface.utils.extensions.getString
import com.topface.topface.utils.extensions.getVipProducts
import com.topface.topface.utils.rx.applySchedulers
import com.topface.topface.utils.rx.safeUnsubscribe
import com.topface.topface.utils.rx.shortSubscription
import rx.Subscription

/**
 * Buy buttons view model
 * Created by petrp on 02.03.2017.
 */
class PaymentNinjaMarketBuyingFragmentViewModel(private val mIsVipPurchaseProducts: Boolean) {
    val isCheckBoxVisible = ObservableInt(View.GONE)
    val isChecked = ObservableBoolean(true)
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

    private var mOptionsSubscription: Subscription? = null

    init {
        mOptionsSubscription = App.getAppComponent().appState().getObservable(Options::class.java)
                .map { it.paymentNinjaInfo }
                .distinctUntilChanged { t1, t2 -> t1 == t2 }
                .applySchedulers()
                .subscribe(shortSubscription {
                    it?.let {
                        if (it.lastDigits.isNotEmpty() && it.type.isNotEmpty()) {
                            cardInfo.set(String.format(R.string.use_card.getString(), it.lastDigits))
                            isCheckBoxVisible.set(View.VISIBLE)
                        } else {
                            isCheckBoxVisible.set(View.GONE)
                        }
                    }
                })
    }

    fun release() {
        mOptionsSubscription.safeUnsubscribe()
    }
}