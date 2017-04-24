package com.topface.topface.ui.fragments.buy.pn_purchase

import android.databinding.ObservableBoolean
import android.databinding.ObservableField
import android.databinding.ObservableInt
import android.view.View
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.data.Options
import com.topface.topface.data.Profile
import com.topface.topface.requests.PaymentNinjaPurchaseRequest
import com.topface.topface.ui.fragments.feed.feed_base.IFeedNavigator
import com.topface.topface.utils.CacheProfile
import com.topface.topface.utils.databinding.MultiObservableArrayList
import com.topface.topface.utils.extensions.*
import com.topface.topface.utils.rx.applySchedulers
import com.topface.topface.utils.rx.safeUnsubscribe
import com.topface.topface.utils.rx.shortSubscription
import rx.Subscription

/**
 * Buy buttons view model
 * Created by petrp on 02.03.2017.
 */
class PaymentNinjaMarketBuyingFragmentViewModel(private val mNavigator: IFeedNavigator,
                                                private val mIsVipPurchaseProducts: Boolean,
                                                private val mFrom: String) {
    val isCheckBoxVisible = ObservableInt(View.GONE)
    val isChecked = ObservableBoolean(true)
    val cardInfo = ObservableField("")
    val data = MultiObservableArrayList<Any>().apply {
        if (mIsVipPurchaseProducts) {
            with(CacheProfile.getPaymentNinjaProductsList().getVipProducts().filter { it.displayOnBuyScreen }) {
                if (size > 0) {
                    this@apply.add(BuyScreenTitle())
                    this@apply.addAll(this)
                } else {
                    this@apply.add(BuyScreenProductUnavailable())
                }
            }

        } else {
            with(Pair(CacheProfile.getPaymentNinjaProductsList().getLikesProducts().filter { it.displayOnBuyScreen },
                    CacheProfile.getPaymentNinjaProductsList().getCoinsProducts().filter { it.displayOnBuyScreen })) {
                if (first.isNotEmpty() || second.isNotEmpty()) {
                    this@apply.add(BuyScreenTitle())
                    if (this@with.first.isNotEmpty()) {
                        this@apply.add(BuyScreenLikesSection())
                        this@apply.addAll(this.first)
                    } else Unit
                    if (this@with.second.isNotEmpty()) {
                        this@apply.add(BuyScreenCoinsSection())
                        this@apply.addAll(this.second)
                    } else Unit
                } else {
                    this@apply.add(BuyScreenProductUnavailable())
                }
            }
        }
    }

    private var mIsTestPurchase = false

    private var mOptionsSubscription: Subscription? = null
    private var mProfileSubscription: Subscription? = null
    private var mEditorSwitchSubscription: Subscription? = null
    private var mPurchaseSubscription: Subscription? = null

    init {
        mProfileSubscription = App.getAppComponent().appState().getObservable(Profile::class.java)
                .map { it.isEditor }
                .distinctUntilChanged()
                .applySchedulers()
                .subscribe(shortSubscription {
                    it?.let {
                        if (it) {
                            if (data.find { it is EditorSwitch } == null) {
                                // находим заголовок списка и после него добавляем переключатель тестовых покупок
                                // ну а если не нашли заголовок, то ставим в начало списка
                                with(data.indexOfFirst { it is BuyScreenTitle } + 1) {
                                    if (data.isEntry(this)) {
                                        data.add(this, EditorSwitch(mIsTestPurchase))
                                    } else {
                                        data.add(EditorSwitch(mIsTestPurchase))
                                    }
                                }
                            } else Unit
                        } else {
                            data.remove(data.find { it is EditorSwitch })
                        }
                    }
                })
        mOptionsSubscription = App.getAppComponent().appState().getObservable(Options::class.java)
                .map { it.paymentNinjaInfo }
                .distinctUntilChanged { t1, t2 -> t1 == t2 }
                .applySchedulers()
                .subscribe(shortSubscription {
                    it?.let {
                        if (it.isCradAvailable()) {
                            cardInfo.set(String.format(R.string.use_card.getString(), it.lastDigits))
                            isCheckBoxVisible.set(View.VISIBLE)
                        } else {
                            isCheckBoxVisible.set(View.GONE)
                        }
                    }
                })
        mEditorSwitchSubscription = App.getAppComponent().eventBus()
                .getObservable(EditorSwitch::class.java)
                .distinctUntilChanged()
                .subscribe(shortSubscription {
                    it?.let { mIsTestPurchase = it.isChecked }
                })
    }

    fun buyProduct(product: PaymentNinjaProduct) {
        if (!App.get().options.paymentNinjaInfo.isCradAvailable() ||
                !isChecked.get()) {
            mNavigator.showPaymentNinjaAddCardScreen(product, mFrom, mIsTestPurchase)
        } else {
            mPurchaseSubscription = PaymentNinjaPurchaseRequest(App.getContext(), product.id, mFrom,
                    mIsTestPurchase, false)
                    .getRequestSubscriber()
                    .applySchedulers()
                    .subscribe(shortSubscription {
                        mNavigator.showPurchaseSuccessfullFragment(product.type)
                    })
        }
    }

    fun release() {
        arrayOf(mOptionsSubscription, mProfileSubscription, mEditorSwitchSubscription, mPurchaseSubscription).safeUnsubscribe()
    }
}