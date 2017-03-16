package com.topface.topface.ui.settings.payment_ninja

import android.os.Looper
import com.topface.topface.App
import com.topface.topface.requests.IApiResponse
import com.topface.topface.requests.RemoveCardRequest
import com.topface.topface.requests.handlers.ApiHandler
import com.topface.topface.ui.settings.payment_ninja.bottom_sheet.BOTTOM_SHEET_ITEMS_POOL
import com.topface.topface.utils.databinding.MultiObservableArrayList
import com.topface.topface.utils.rx.applySchedulers
import com.topface.topface.utils.rx.safeUnsubscribe
import com.topface.topface.utils.rx.shortSubscription
import rx.Emitter
import rx.Observable
import rx.Subscription
import java.util.concurrent.TimeUnit

/**
 * ViewModel for current users cards and subscriptions
 * Created by ppavlik on 06.03.17.
 */

class SettingsPaymentNinjaViewModel {

    private var mRequestSubscription: Subscription? = null
    private var mBottomSheetClickSubscription: Subscription? = null
    private var mDeleteCardSubscription: Subscription? = null

    val data: MultiObservableArrayList<Any> by lazy {
        MultiObservableArrayList<Any>()
    }

    private val mEventBus by lazy {
        App.getAppComponent().eventBus()
    }

    init {
        data.replaceData(arrayListOf<Any>(PaymnetNinjaPurchasesLoader()))
        sendRequest()
        mBottomSheetClickSubscription = mEventBus.getObservable(BOTTOM_SHEET_ITEMS_POOL::class.java)
                .applySchedulers()
                .subscribe(shortSubscription {
                    it?.let {
                        when (it) {
                            BOTTOM_SHEET_ITEMS_POOL.CANCEL_SUBSCRIPTION -> TODO()
                            BOTTOM_SHEET_ITEMS_POOL.RESUME_SUBSCRIPTION -> TODO()
                            BOTTOM_SHEET_ITEMS_POOL.DELETE_CARD -> deleteCard()
                            BOTTOM_SHEET_ITEMS_POOL.USE_ANOTHER_CARD -> TODO()
                        }
                    }
                })
    }

    private fun deleteCard() {
        mDeleteCardSubscription = getDeleteCardRequest()
                .applySchedulers()
                .subscribe({

                },{

                })
    }

    private fun getDeleteCardRequest() =
            Observable.fromEmitter<IApiResponse>({ emitter ->
                val sendRequest = RemoveCardRequest(App.getContext())
                sendRequest.callback(object : ApiHandler(Looper.getMainLooper()) {
                    override fun success(response: IApiResponse) = emitter.onNext(response)
                    override fun fail(codeError: Int, response: IApiResponse) = emitter.onError(Throwable(codeError.toString()))
                    override fun always(response: IApiResponse) {
                        super.always(response)
                        emitter.onCompleted()
                    }
                }).exec()
            }, Emitter.BackpressureMode.LATEST)

    private fun sendRequest() {
        mRequestSubscription = Observable.timer(2, TimeUnit.SECONDS)
                .applySchedulers()
                .subscribe(shortSubscription {
                    data.replaceData(arrayListOf(CardInfo("1234", "Maestro"),
                            SubscriptionInfo("some id", 0, "Подписка на ВИП", 1493683200, true),
                            SubscriptionInfo("some id", 1, "Автопополнение монет", 1493683200, true),
                            PaymentNinjaHelp()))
                })
    }

    fun release() {
        mRequestSubscription.safeUnsubscribe()
        mBottomSheetClickSubscription.safeUnsubscribe()
    }
}