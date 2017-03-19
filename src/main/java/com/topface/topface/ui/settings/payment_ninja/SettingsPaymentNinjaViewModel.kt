package com.topface.topface.ui.settings.payment_ninja

import android.os.Looper
import com.topface.framework.JsonUtils
import com.topface.topface.App
import com.topface.topface.requests.*
import com.topface.topface.requests.response.SimpleResponse
import com.topface.topface.ui.fragments.feed.feed_base.FeedNavigator
import com.topface.topface.ui.settings.payment_ninja.bottom_sheet.BottomSheetData
import com.topface.topface.ui.settings.payment_ninja.bottom_sheet.BottomSheetItemText
import com.topface.topface.utils.databinding.MultiObservableArrayList
import com.topface.topface.utils.extensions.isAvailable
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

class SettingsPaymentNinjaViewModel(private val mNavigator: FeedNavigator) {

    private var mRequestSubscription: Subscription? = null
    private var mBottomSheetClickSubscription: Subscription? = null
    private var mDeleteCardSubscription: Subscription? = null
    private var mCancelSubscription: Subscription? = null
    private var mResumeSubscription: Subscription? = null
    private var mDefaultCardRequestSubscription: Subscription? = null
    private var mUserSubscriptionsRequestSubscription: Subscription? = null

    private val data = MultiObservableArrayList<Any>()

    private val mEventBus by lazy {
        App.getAppComponent().eventBus()
    }

    init {
        mBottomSheetClickSubscription = mEventBus.getObservable(BottomSheetData::class.java)
                .applySchedulers()
                .subscribe(shortSubscription {
                    it?.let {
                        when (it.textRes.textRes) {
                            BottomSheetItemText.CANCEL_SUBSCRIPTION -> cancelSubscriptionRequest(it.data as SubscriptionInfo)
                            BottomSheetItemText.RESUME_SUBSCRIPTION -> resumeSubscriptionRequest(it.data as SubscriptionInfo)
                            BottomSheetItemText.DELETE_CARD -> deleteCardRequest()
                            BottomSheetItemText.USE_ANOTHER_CARD, BottomSheetItemText.ADD_CARD -> mNavigator.showPaymentNinjaPurchaseProduct()
                        }
                    }
                })
        getData().replaceData(arrayListOf<Any>(PaymnetNinjaPurchasesLoader()))
        sendCardListRequest()
        sendUserSubscriptionsRequest()
    }

    @Synchronized
    fun getData() = data

    private fun deleteCardRequest() {
        mDeleteCardSubscription = getDeleteCardRequest()
                .applySchedulers()
                .subscribe(shortSubscription {
                    data.set(with(getData().indexOfFirst { it is CardInfo }) {
                        if (this < 0) 0 else this
                    }, CardInfo())
                })
    }

    private fun resumeSubscriptionRequest(subscriptionInfo: SubscriptionInfo) {
        mResumeSubscription = getResumeSubscriptionRequest()
                .applySchedulers()
                .subscribe(shortSubscription {
                    data.find { it == subscriptionInfo }?.let {
                        val position = data.indexOf(it)
                        data.set(position, (it as SubscriptionInfo).apply { enabled = true })
                    }
                })
    }

    private fun cancelSubscriptionRequest(subscriptionInfo: SubscriptionInfo) {
        mCancelSubscription = getCancelSubscriptionRequest()
                .applySchedulers()
                .subscribe(shortSubscription {
                    data.find { it == subscriptionInfo }?.let {
                        val position = data.indexOf(it)
                        data.set(position, (it as SubscriptionInfo).apply { enabled = false })
                    }
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

    private fun getCancelSubscriptionRequest() =
            Observable.fromEmitter<SimpleResponse>({ emitter ->
                val sendRequest = CancelSubscriptionRequest(App.getContext())
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

    private fun getResumeSubscriptionRequest() =
            Observable.fromEmitter<SimpleResponse>({ emitter ->
                val sendRequest = ResumeSubscriptionRequest(App.getContext())
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

    // при получении новых данных карты производим удаление текущей и в нулевую позицию добавляем текущую
    private fun replaceCardData(card: CardInfo) {
        val list = getData().getList()
        list.removeAll(list.filter { it is CardInfo })
        list.add(0, card)
        getData().replaceData(list)
    }

    // при получении новых подписок сначала удалим все подписки из списка, потом добавим текущие после
    // карты или в нулевую позицию, если карты еще нет
    private fun replaceSubscriptionsData(subscriptions: UserSubscriptions) {
        val list = getData().getList()
        list.removeAll(list.filter { it is SubscriptionInfo })
        list.addAll(list.indexOfLast { it is CardInfo } + 1, subscriptions.userSubscriptions.asList())
        getData().replaceData(list)
    }

    // ищем лоадер в списке и удаляем его если смогли найти
    private fun removeLoader() = getData().removeAll(getData().filter { it is PaymnetNinjaPurchasesLoader })

    // если в списке еще нет пункта о помощи, то добавляем его
    private fun addHelpItem() {
        if (getData().find { it is PaymentNinjaHelp } == null) {
            getData().add(PaymentNinjaHelp())
        }
    }

    fun isCardAvailable() =
            getData().find { it is CardInfo }?.let { (it as? CardInfo)?.isAvailable() }

    fun getCardInfo() =
            getData().find { it is CardInfo }?.let { (it as? CardInfo) }

    fun release() {
        arrayOf(mDefaultCardRequestSubscription, mRequestSubscription,
                mBottomSheetClickSubscription, mDeleteCardSubscription,
                mUserSubscriptionsRequestSubscription, mResumeSubscription,
                mCancelSubscription).safeUnsubscribe()
    }
}