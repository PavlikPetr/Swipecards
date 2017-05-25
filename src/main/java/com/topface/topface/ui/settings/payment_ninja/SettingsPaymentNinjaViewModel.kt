package com.topface.topface.ui.settings.payment_ninja

import android.app.Activity
import android.content.Intent
import android.os.Looper
import com.topface.billing.ninja.NinjaAddCardActivity
import com.topface.framework.JsonUtils
import com.topface.topface.App
import com.topface.topface.requests.*
import com.topface.topface.requests.response.SimpleResponse
import com.topface.topface.ui.fragments.feed.feed_base.FeedNavigator
import com.topface.topface.ui.settings.payment_ninja.bottom_sheet.BottomSheetData
import com.topface.topface.ui.settings.payment_ninja.bottom_sheet.BottomSheetItemText
import com.topface.topface.utils.ILifeCycle
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

class SettingsPaymentNinjaViewModel(private val mNavigator: FeedNavigator,
                                    private val mAlertDialog: IAlertDialog) : ILifeCycle {

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
                            BottomSheetItemText.CANCEL_SUBSCRIPTION -> cancelSubscriptionRequest(it.data as? SubscriptionInfo)
                            BottomSheetItemText.DELETE_CARD -> mAlertDialog.show { deleteCardRequest(it.data as? CardInfo) }
                            BottomSheetItemText.USE_ANOTHER_CARD, BottomSheetItemText.ADD_CARD -> mNavigator
                                    .showPaymentNinjaAddCardScreen(source = SettingsPaymentsNinjaFragment.FROM)
                            else -> {
                            }
                        }
                    }
                })
        getData().replaceData(arrayListOf<Any>(PaymnetNinjaPurchasesLoader()))
        sendCardListRequest()
        sendUserSubscriptionsRequest()
    }

    @Synchronized
    fun getData() = data

    private fun deleteCardRequest(cardInfo: CardInfo?) {
        cardInfo?.let { card ->
            getData().indexOf(card).takeIf { it != -1 }?.let {
                getData().set(it, CardInfo())
            }
            mDeleteCardSubscription = getDeleteCardRequest()
                    .applySchedulers()
                    .subscribe({
                        // в случае успешного удаления карты необходимо обновить опции пользователя
                        App.getUserOptionsRequest().exec()
                        sendCardListRequest()
                        sendUserSubscriptionsRequest()
                    }, {
                        sendCardListRequest()
                        sendUserSubscriptionsRequest()
                    })
        }
    }

    private fun cancelSubscriptionRequest(subscriptionInfo: SubscriptionInfo?) =
            subscriptionInfo?.let { subscription ->
                if (subscriptionInfo.type == SubscriptionInfo.SUBSCRIPTION_TYPE_AUTO_REFILL) {
                    getData().remove(subscription)
                } else {
                    getData().indexOf(subscription).takeIf { it != -1 }?.let {
                        getData().set(it, subscription.copy().apply { enabled = false })
                    }
                }
                mCancelSubscription = getCancelSubscriptionRequest(subscription.type)
                        .applySchedulers()
                        .subscribe({ sendUserSubscriptionsRequest() }, { sendUserSubscriptionsRequest() })
            }

    private fun getSubscriptionsRequest() =
            Observable.fromEmitter<UserSubscriptions>({ emitter ->
                val sendRequest = PaymentNinhaSubscriptionsRequest(App.getContext())
                sendRequest.callback(object : DataApiHandler<UserSubscriptions>(Looper.getMainLooper()) {
                    override fun success(data: UserSubscriptions?, response: IApiResponse?) = emitter.onNext(data)
                    override fun parseResponse(response: ApiResponse?) = JsonUtils.fromJson(response.toString(), UserSubscriptions::class.java)
                    override fun fail(codeError: Int, response: IApiResponse) {
                        emitter.onError(Throwable(codeError.toString()))
                    }

                    override fun always(response: IApiResponse) {
                        super.always(response)
                        emitter.onCompleted()
                    }
                }).exec()
            }, Emitter.BackpressureMode.LATEST)

    private fun getDefaultCardRequest() =
            Observable.fromEmitter<CardInfo>({ emitter ->
                val sendRequest = PaymentNinjaGetCardRequest(App.getContext())
                sendRequest.callback(object : DataApiHandler<CardInfo>(Looper.getMainLooper()) {
                    override fun success(data: CardInfo?, response: IApiResponse?) = emitter.onNext(data)
                    override fun parseResponse(response: ApiResponse?) = JsonUtils.fromJson(response.toString(), CardInfo::class.java)
                    override fun fail(codeError: Int, response: IApiResponse) {
                        emitter.onError(Throwable(codeError.toString()))
                    }

                    override fun always(response: IApiResponse) {
                        super.always(response)
                        emitter.onCompleted()
                    }
                }).exec()
            }, Emitter.BackpressureMode.LATEST)

    private fun getCancelSubscriptionRequest(type: String) =
            Observable.fromEmitter<SimpleResponse>({ emitter ->
                val sendRequest = CancelSubscriptionRequest(App.getContext(), type)
                sendRequest.callback(object : DataApiHandler<SimpleResponse>(Looper.getMainLooper()) {
                    override fun success(data: SimpleResponse?, response: IApiResponse?) = emitter.onNext(data)
                    override fun parseResponse(response: ApiResponse?) = JsonUtils.fromJson(response.toString(), SimpleResponse::class.java)
                    override fun fail(codeError: Int, response: IApiResponse) {
                        emitter.onError(Throwable(codeError.toString()))
                    }

                    override fun always(response: IApiResponse) {
                        super.always(response)
                        emitter.onCompleted()
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
                        emitter.onError(Throwable(codeError.toString()))
                    }

                    override fun always(response: IApiResponse) {
                        super.always(response)
                        emitter.onCompleted()
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
                        // отправляем запрос новых опций, т.к. успешно была добавлена карта
                        App.getUserOptionsRequest().exec()
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

    fun getCardInfo() =
            getData().find { it is CardInfo }?.let { (it as? CardInfo) }

    fun release() {
        arrayOf(mDefaultCardRequestSubscription, mRequestSubscription,
                mBottomSheetClickSubscription, mDeleteCardSubscription,
                mUserSubscriptionsRequestSubscription, mResumeSubscription,
                mCancelSubscription).safeUnsubscribe()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // если пришел интент о закрытии активити добавления карты,
        // то шлем запрос на получение default карты
        if (requestCode == NinjaAddCardActivity.REQUEST_CODE &&
                resultCode == Activity.RESULT_OK &&
                data?.getBooleanExtra(NinjaAddCardActivity.CARD_SENDED_SUCCESFULL, false) ?: false) {
            sendCardListRequest()
            sendUserSubscriptionsRequest()
        }
    }
}