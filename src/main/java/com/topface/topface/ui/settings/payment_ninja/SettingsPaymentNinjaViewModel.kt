package com.topface.topface.ui.settings.payment_ninja

import android.os.Looper
import com.topface.framework.JsonUtils
import com.topface.topface.App
import com.topface.topface.requests.*
import com.topface.topface.requests.response.SimpleResponse
import com.topface.topface.ui.settings.payment_ninja.bottom_sheet.BOTTOM_SHEET_ITEMS_POOL
import com.topface.topface.utils.databinding.MultiObservableArrayList
import com.topface.topface.utils.rx.applySchedulers
import com.topface.topface.utils.rx.safeUnsubscribe
import com.topface.topface.utils.rx.shortSubscription
import rx.Emitter
import rx.Observable
import rx.Subscription

/**
 * ViewModel for current users cards and subscriptions
 * Created by ppavlik on 06.03.17.
 */

class SettingsPaymentNinjaViewModel {

    private var mRequestSubscription: Subscription? = null
    private var mBottomSheetClickSubscription: Subscription? = null
    private var mDeleteCardSubscription: Subscription? = null
    private var mDefaultCardRequestSubscription: Subscription? = null
    private var mUserSubscriptionsRequestSubscription: Subscription? = null

    val data: MultiObservableArrayList<Any> by lazy {
        MultiObservableArrayList<Any>()
    }

    private val mEventBus by lazy {
        App.getAppComponent().eventBus()
    }

    init {
        mBottomSheetClickSubscription = mEventBus.getObservable(BOTTOM_SHEET_ITEMS_POOL::class.java)
                .applySchedulers()
                .subscribe(shortSubscription {
                    it?.let {
                        when (it) {
                            BOTTOM_SHEET_ITEMS_POOL.CANCEL_SUBSCRIPTION -> TODO()
                            BOTTOM_SHEET_ITEMS_POOL.RESUME_SUBSCRIPTION -> TODO()
                            BOTTOM_SHEET_ITEMS_POOL.DELETE_CARD -> deleteCardRequest()
                            BOTTOM_SHEET_ITEMS_POOL.USE_ANOTHER_CARD -> TODO()
                        }
                    }
                })
        data.replaceData(arrayListOf<Any>(PaymnetNinjaPurchasesLoader()))
        sendCardListRequest()
        sendUserSubscriptionsRequest()
    }

    private fun deleteCardRequest() {
        mDeleteCardSubscription = getDeleteCardRequest()
                .applySchedulers()
                .subscribe(shortSubscription {
                    data[with(data.indexOfFirst { it is CardInfo }) {
                        if (this < 0) 0 else this
                    }] = CardInfo()
                })
    }


    private fun getSubscriptionsRequest() =
            Observable.fromEmitter<UserSubscriptions>({ emitter ->
                emitter.onNext(UserSubscriptions(arrayOf(
                        SubscriptionInfo("some id", 0, "Подписка на ВИП", 1493683200, true),
                        SubscriptionInfo("some id", 1, "Автопополнение монет", 1493683200, true))))
                val sendRequest = PaymentNinhaSubscriptionsRequest(App.getContext())
                sendRequest.callback(object : DataApiHandler<UserSubscriptions>(Looper.getMainLooper()) {
                    override fun success(data: UserSubscriptions?, response: IApiResponse?) = emitter.onNext(data)
                    override fun parseResponse(response: ApiResponse?) = JsonUtils.fromJson(response.toString(), UserSubscriptions::class.java)
                    override fun fail(codeError: Int, response: IApiResponse) {
//                        emitter.onError(Throwable(codeError.toString()))
                    }

                    override fun always(response: IApiResponse) {
                        super.always(response)
                        emitter.onNext(UserSubscriptions(arrayOf(
                                SubscriptionInfo("some id", 0, "Подписка на ВИП", 1493683200, true),
                                SubscriptionInfo("some id", 1, "Автопополнение монет", 1493683200, true))))
//                        emitter.onCompleted()
                    }
                }).exec()
            }, Emitter.BackpressureMode.LATEST)

    private fun getDefaultCardRequest() =
            Observable.fromEmitter<CardInfo>({ emitter ->
                emitter.onNext(CardInfo("8745", "Visa"))
                val sendRequest = DefaultCardRequest(App.getContext())
                sendRequest.callback(object : DataApiHandler<CardInfo>(Looper.getMainLooper()) {
                    override fun success(data: CardInfo?, response: IApiResponse?) = emitter.onNext(data)
                    override fun parseResponse(response: ApiResponse?) = JsonUtils.fromJson(response.toString(), CardInfo::class.java)
                    override fun fail(codeError: Int, response: IApiResponse) {
//                        emitter.onError(Throwable(codeError.toString()))
                    }

                    override fun always(response: IApiResponse) {
                        super.always(response)
                        emitter.onNext(CardInfo("8745", "Visa"))
//                        emitter.onCompleted()
                    }
                }).exec()
            }, Emitter.BackpressureMode.LATEST)

    private fun getDeleteCardRequest() =
            Observable.fromEmitter<SimpleResponse>({ emitter ->
                val sendRequest = RemoveCardRequest(App.getContext())
                sendRequest.callback(object : DataApiHandler<SimpleResponse>(Looper.getMainLooper()) {
                    override fun success(data: SimpleResponse?, response: IApiResponse?) = emitter.onNext(data)
                    override fun parseResponse(response: ApiResponse?) = JsonUtils.fromJson(response.toString(), SimpleResponse::class.java)
                    override fun fail(codeError: Int, response: IApiResponse) {
//                        emitter.onError(Throwable(codeError.toString()))
                    }

                    override fun always(response: IApiResponse) {
                        super.always(response)
                        emitter.onNext(SimpleResponse(true))
//                        emitter.onCompleted()
                    }
                }).exec()
            }, Emitter.BackpressureMode.LATEST)

    private fun sendCardListRequest() {
        mDefaultCardRequestSubscription = getDefaultCardRequest().applySchedulers()
                .subscribe(shortSubscription {
                    it?.let {
                        removeLoader()
                        replaceCardData(it)
                        addHelpItem()
                    }
                })
    }

    private fun sendUserSubscriptionsRequest() {
        mUserSubscriptionsRequestSubscription = getSubscriptionsRequest().applySchedulers()
                .subscribe(shortSubscription {
                    it?.let {
                        removeLoader()
                        replaceSubscriptionsData(it)
                        addHelpItem()
                    }
                })
    }

    private fun replaceCardData(card: CardInfo) {
        val list = data.getList()
        list.removeAll(list.filter { it is CardInfo })
        list.add(0, card)
        data.replaceData(list)
    }

    private fun replaceSubscriptionsData(subscriptions: UserSubscriptions) {
        val list = data.getList()
        list.removeAll(list.filter { it is SubscriptionInfo })
        list.addAll(0, subscriptions.userSubscriptions.asList())
        data.replaceData(list)
    }

    private fun removeLoader() = data.removeAll(data.filter { it is PaymnetNinjaPurchasesLoader })

    private fun addHelpItem() {
        if (data.find { it is PaymentNinjaHelp } == null) {
            data.add(PaymentNinjaHelp())
        }
    }


//    private fun sendRequest() {
//        mRequestSubscription = Observable.timer(2, TimeUnit.SECONDS)
//                .applySchedulers()
//                .subscribe(shortSubscription {
//                    data.replaceData(arrayListOf(CardInfo("1234", "Maestro"),
//                            SubscriptionInfo("some id", 0, "Подписка на ВИП", 1493683200, true),
//                            SubscriptionInfo("some id", 1, "Автопополнение монет", 1493683200, true),
//                            PaymentNinjaHelp()))
//                })
//    }

    fun release() {
        arrayOf(mDefaultCardRequestSubscription, mRequestSubscription,
                mBottomSheetClickSubscription, mDeleteCardSubscription,
                mUserSubscriptionsRequestSubscription).safeUnsubscribe()
    }
}