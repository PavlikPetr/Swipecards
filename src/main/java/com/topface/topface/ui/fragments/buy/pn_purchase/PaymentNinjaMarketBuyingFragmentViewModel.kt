package com.topface.topface.ui.fragments.buy.pn_purchase

import android.databinding.ObservableBoolean
import android.databinding.ObservableField
import android.databinding.ObservableInt
import android.view.View
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.data.Options
import com.topface.topface.requests.PaymentNinjaPurchaseRequest
import com.topface.topface.ui.fragments.feed.feed_base.FeedNavigator
import com.topface.topface.utils.CacheProfile
import com.topface.topface.utils.databinding.SingleObservableArrayList
import com.topface.topface.utils.extensions.*
import com.topface.topface.utils.rx.applySchedulers
import com.topface.topface.utils.rx.safeUnsubscribe
import com.topface.topface.utils.rx.shortSubscription
import rx.Subscription

/**
 * Buy buttons view model
 * Created by petrp on 02.03.2017.
 */
class PaymentNinjaMarketBuyingFragmentViewModel(private val mNavigator: FeedNavigator, private val mIsVipPurchaseProducts: Boolean, private val mFrom: String) {
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
    private var mPurchaseSubscription: Subscription? = null

    init {
        mOptionsSubscription = App.getAppComponent().appState().getObservable(Options::class.java)
                .map { it.paymentNinjaInfo }
                .distinctUntilChanged { t1, t2 -> t1 == t2 }
                .applySchedulers()
                .subscribe(shortSubscription {
                    it?.let {
                        if (it.isCradAvailable()) {
                            cardInfo.set(String.format(R.string.use_card.getString(), it.lastDigit))
                            isCheckBoxVisible.set(View.VISIBLE)
                        } else {
                            isCheckBoxVisible.set(View.GONE)
                        }
                    }
                })
    }

    fun buyProduct(product: PaymentNinjaProduct) {
        if (!App.get().options.paymentNinjaInfo.isCradAvailable() ||
                !isChecked.get()) {
            mNavigator.showPaymentNinjaAddCardScreen(product, mFrom)
        } else {
            mPurchaseSubscription = PaymentNinjaPurchaseRequest(App.getContext(), product.id, mFrom)
                    .getRequestSubscriber()
                    .applySchedulers()
                    .subscribe(shortSubscription {
                        mNavigator.showPurchaseSuccessfullFragment(product.type)
                    })
        }
    }

    fun release() {
        arrayOf(mPurchaseSubscription, mOptionsSubscription).safeUnsubscribe()
    }
}