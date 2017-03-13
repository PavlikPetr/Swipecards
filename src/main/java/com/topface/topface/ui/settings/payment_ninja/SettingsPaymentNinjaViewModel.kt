package com.topface.topface.ui.settings.payment_ninja

import com.topface.topface.utils.databinding.MultiObservableArrayList
import com.topface.topface.utils.rx.applySchedulers
import com.topface.topface.utils.rx.safeUnsubscribe
import com.topface.topface.utils.rx.shortSubscription
import rx.Observable
import rx.Subscription
import java.util.concurrent.TimeUnit

/**
 * ViewModel for current users cards and subscriptions
 * Created by ppavlik on 06.03.17.
 */

class SettingsPaymentNinjaViewModel {

    private var mRequestSubscription: Subscription? = null

    val data: MultiObservableArrayList<Any> by lazy {
        MultiObservableArrayList<Any>()
    }

    init {
        data.replaceData(arrayListOf<Any>(PaymnetNinjaPurchasesLoader()))
        sendRequest()
    }

    private fun sendRequest() {
        mRequestSubscription = Observable.timer(2, TimeUnit.SECONDS)
                .applySchedulers()
                .subscribe(shortSubscription {
                    data.replaceData(arrayListOf<Any>(CardInfo("1234", "Maestro"),
                            SubscriptionInfo("some id", 0, "Подписка на ВИП", 1493683200, true),
                            SubscriptionInfo("some id", 1, "Автопополнение монет", 1493683200, true),
                            PaymentNinjaHelp()))
                })
    }

    fun release() {
        mRequestSubscription.safeUnsubscribe()
    }
}