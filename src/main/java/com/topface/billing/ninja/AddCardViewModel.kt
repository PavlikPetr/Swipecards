package com.topface.billing.ninja

import android.app.Activity
import android.content.Intent
import android.databinding.Observable
import android.databinding.ObservableBoolean
import android.databinding.ObservableField
import android.databinding.ObservableInt
import android.os.Bundle
import android.os.Looper
import android.text.TextUtils
import android.view.View
import com.topface.billing.ninja.CardUtils.UtilsForCard
import com.topface.billing.ninja.CardUtils.UtilsForCard.EMAIL_ADDRESS
import com.topface.billing.ninja.CardUtils.UtilsForCard.INPUT_DELAY
import com.topface.framework.JsonUtils
import com.topface.framework.utils.Debug
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.data.Products
import com.topface.topface.requests.ApiResponse
import com.topface.topface.requests.DataApiHandler
import com.topface.topface.requests.IApiResponse
import com.topface.topface.requests.PaymentNinjaPurchaseRequest
import com.topface.topface.requests.response.SimpleResponse
import com.topface.topface.ui.fragments.buy.pn_purchase.PaymentNinjaProduct
import com.topface.topface.ui.fragments.feed.feed_base.FeedNavigator
import com.topface.topface.utils.Utils
import com.topface.topface.utils.extensions.getString
import com.topface.topface.utils.rx.*
import rx.Emitter
import rx.Observable.fromEmitter
import rx.Subscription
import rx.subscriptions.CompositeSubscription
import java.util.*
import java.util.concurrent.TimeUnit


/**
 * ВьюМодель добавления карт
 */

class AddCardViewModel(val data: Bundle, val mFinishCallback: IFinishDelegate) {

    val numberText = RxFieldObservable<String>()
    val numberCursorPosition = ObservableInt()
    val numberMaxLength = ObservableInt(19)
    val cardIcon = ObservableInt()
    val numberError = ObservableField<String>()

    private var mPurchaseRequestSubscription: Subscription? = null

    val cvvChangedCallback = object : Observable.OnPropertyChangedCallback() {
        override fun onPropertyChanged(observable: Observable?, p1: Int) = observable?.let {
            with(it as ObservableField<String>) {
                if (!get().isNullOrEmpty()) {
                    if (get().length < cvvMaxLength.get() || !UtilsForCard.isDigits(get())) {
                        Debug.error("--------------------Все очень плохо-----слишком мало букав---или введен текст-------------------")
                        cvvError.set(R.string.ninja_cvv_error.getString())
                        readyCheck.put(cvvText, false)
                    } else {
                        cvvError.set(Utils.EMPTY)
                        readyCheck.put(cvvText, true)
                    }
                } else {
                    Debug.error("---------------------ОШИБКА заполните поле cvv -----------------------")
                    cvvError.set(R.string.ninja_cvv_error.getString())
                    readyCheck.put(cvvText, false)
                }
                updateButton()
            }
        } ?: Unit
    }

    val cvvText = ObservableField<String>()
    val cvvMaxLength = ObservableInt(3)
    val cvvError = ObservableField<String>()

    val trhuText = RxFieldObservable<String>()
    val trhuCursorPosition = ObservableInt()
    val trhuError = ObservableField<String>()

    val emailChangedCallback = object : Observable.OnPropertyChangedCallback() {
        override fun onPropertyChanged(observable: Observable?, p1: Int) = observable?.let {
            with(observable as ObservableField<String>) {
                if (!get().isNullOrEmpty()) {
                    if (!get().matches(EMAIL_ADDRESS.toRegex())) {
                        Debug.error("--------------------EMAIL невалидный-----------------------------")
                        emailError.set(R.string.ninja_email_error.getString())
                        readyCheck.put(emailText, false)
                    } else {
                        emailError.set(Utils.EMPTY)
                        readyCheck.put(emailText, true)
                    }
                } else {
                    Debug.error("---------------------ОШИБКА заполните поле email -----------------------")
                    emailError.set(R.string.ninja_email_error.getString())
                    readyCheck.put(emailText, false)
                }
                updateButton()
            }
        } ?: Unit
    }

    val emailText = ObservableField<String>()
    val emailError = ObservableField<String>()

    val cardFieldsSubscription = CompositeSubscription()

    val productTitle = ObservableField<String>()

    val isAutoPayDescriptionVisible = ObservableBoolean(false)
    val autoPayDescriptionText = ObservableField<String>()
    val isVipDescriptionVisible = ObservableBoolean(false)
    val vipDescriptionText = ObservableField<String>()
    val isEmailFormNeeded = ObservableBoolean(false)

    val isButtonEnabled = ObservableBoolean(false)
    val isInputEnabled = ObservableBoolean(true)
    val titleVisibility = ObservableInt(View.GONE)

    var mFeedNavigator: FeedNavigator? = null

    val product: PaymentNinjaProduct? = data.getParcelable(NinjaAddCardActivity.EXTRA_BUY_PRODUCT)

    private val readyCheck: MutableMap<Any, Boolean> = mutableMapOf()

    init {
        val email = App.get().options.paymentNinjaInfo.email
        val isEmailDefined = TextUtils.isEmpty(email)
        readyCheck.apply {
            put(numberText, false)
            put(cvvText, false)
            put(trhuText, false)
            put(emailText, !isEmailDefined)
        }
        product?.let {
            productTitle.set(it.titleTemplate)
            titleVisibility.set(View.VISIBLE)
            autoPayDescriptionText.set(it.infoOfSubscription.text)
            isAutoPayDescriptionVisible.set(it.type == Products.ProductType.COINS.getName() && it.typeOfSubscription == 1)
            isVipDescriptionVisible.set(it.type == Products.ProductType.PREMIUM.getName())
            // todo possibly add second text with template
            vipDescriptionText.set(R.string.ninja_text_5.getString())
        }
        isEmailFormNeeded.set(TextUtils.isEmpty(email))
        cvvText.addOnPropertyChangedCallback(cvvChangedCallback)
        emailText.addOnPropertyChangedCallback(emailChangedCallback)
        if (!isEmailDefined) {
            emailText.set(email)
        }

        cardFieldsSubscription.add(numberText.filedObservable
                .doOnNext {
                    if (!it.isNullOrEmpty() && UtilsForCard.isDigits(it.replace(UtilsForCard.SPACE_DIVIDER, ""))) {
                        // валидация по алгоритму Луна
                        if (!UtilsForCard.luhnsAlgorithm(it.replace(UtilsForCard.SPACE_DIVIDER, ""))) {
                            Debug.error("---------------------ОШИБКА ввода номера карты--------------------------------")
                            numberError.set(R.string.ninja_number_error.getString())
                            readyCheck.put(numberText, false)
                        } else {
                            numberError.set(Utils.EMPTY)
                            readyCheck.put(numberText, true)
                        }
                    } else {
                        Debug.error("---------------------ОШИБКА заполните поле ввода номера карты -----------------------")
                        numberError.set(R.string.ninja_number_error.getString())
                        readyCheck.put(numberText, false)
                    }
                    updateButton()
                }
                .filter {
                    numberCursorPosition.set(it.length)
                    it.length >= 4
                }
                .distinctUntilChanged()
                .throttleLast(INPUT_DELAY, TimeUnit.MILLISECONDS)
                .map { str -> UtilsForCard.formattingForCardNumber(str) }
                .subscribe(shortSubscription {
                    it?.let {
                        if (it.length == 4) {
                            val cardType = giveMeBrand(it, UtilsForCard.cardBrands)
                            setTemplate(cardType)
                        }
                        setNumber(it)
                    }
                }))

        cardFieldsSubscription.add(trhuText.filedObservable
                .doOnNext {
                    if (!it.isNullOrEmpty() && it.length == UtilsForCard.TRHU_LENGTH) {
                        if (!UtilsForCard.isValidTrhu(it)) {
                            Debug.error("---------------------ОШИБКА ввода Срока годности карты------------------------")
                            trhuError.set(R.string.ninja_trhu_error.getString())
                            readyCheck.put(trhuText, false)
                        } else {
                            trhuError.set(Utils.EMPTY)
                            readyCheck.put(trhuText, true)
                        }
                    } else {
                        trhuError.set(R.string.ninja_trhu_error.getString())
                        readyCheck.put(trhuText, false)
                    }
                    updateButton()
                }
                .filter { it.length >= 2 }
                .distinctUntilChanged()
                .map { str -> UtilsForCard.setTrhuDivider(str) }
                .throttleLast(INPUT_DELAY, TimeUnit.MILLISECONDS)
                .subscribe(shortSubscription {
                    it?.let {
                        setTRHU(it)
                    }
                })
        )
    }

    private fun updateButton() {
        isButtonEnabled.set(!readyCheck.containsValue(false))
    }

    fun setFeedNavigator(feedNavigator: FeedNavigator) = this.apply { mFeedNavigator = feedNavigator }

    private fun setTemplate(cardType: CardType) {
        numberMaxLength.set(cardType.numberMaxLength)
        cvvMaxLength.set(cardType.cvvMaxLength)
        cardIcon.set(cardType.cardIcon)
    }

    private fun giveMeBrand(cardNumber: String, cardBrands: HashMap<Regex, CardType>): CardType {
        for (cardRegex in cardBrands.keys) {
            if (cardNumber.matches(cardRegex)) {
                return cardBrands.get(cardRegex)!!
            }
        }
        return CardType.DEFAULT
    }

    fun setNumber(number: String) {
        numberText.set(number)
    }

    fun setTRHU(trhu: String) {
        trhuText.set(trhu)
        trhuCursorPosition.set(trhu.length)
    }

    fun release() {
        cardFieldsSubscription.clear()
        cvvText.removeOnPropertyChangedCallback(cvvChangedCallback)
        emailText.removeOnPropertyChangedCallback(emailChangedCallback)
        mPurchaseRequestSubscription.safeUnsubscribe()
    }

    fun onClick() {
        val trhuString = trhuText.get()
        var month: String = ""
        var year: String = ""
        if (!TextUtils.isEmpty(trhuString) && UtilsForCard.isValidTrhu(trhuString)) {
            month = trhuString.substring(0, 2)
            year = trhuString.substring(3)
        }
        val cardModel = AddCardModel(
                App.get().options.paymentNinjaInfo.publicKey,
                numberText.get()?.replace(UtilsForCard.SPACE_DIVIDER, "") ?: "",
                month,
                year,
                cvvText.get() ?: "",
                emailText.get() ?: ""
        )
        isInputEnabled.set(false)
        AddCardRequest().getRequestObservable(App.get(), cardModel)
                .applySchedulers()
                .subscribe(object : RxUtils.ShortSubscription<IApiResponse>() {
                    override fun onCompleted() {
                        super.onCompleted()
                        isInputEnabled.set(true)
                    }

                    override fun onError(e: Throwable?) {
                        super.onError(e)
                        mFeedNavigator?.showPaymentNinjaErrorDialog(data.getBoolean(NinjaAddCardActivity.EXTRA_FROM_INSTANT_PURCHASE) ||
                                product == null) {
                            if (isEmailFormNeeded.get()) {
                                emailText.set("")
                            }
                            numberText.set("")
                            cvvText.set("")
                            trhuText.set("")
                        }
                        isInputEnabled.set(true)
                    }

                    override fun onNext(t: IApiResponse?) {
                        // todo send "buy payment ninja product" here and after success show dialog
                        // если есть продукт, значит надо провести покупку. Ориентируясь на успешность этого
                        // запроса на сервер покажем экран успешной покупки mFeedNavigator?.showPurchaseSuccessfullFragment(it.type)
                        // если продукт null, значит закрываем активити, но при этом не забываем сообщить о том, что карта добавлена
                        // успешно
                        product?.let {
                            mFeedNavigator?.showPurchaseSuccessfullFragment(it.type)
//                            sendPurchaseRequest(cardModel.email, "", it.type)
                        } ?: mFinishCallback.finishWithResult(Activity.RESULT_OK,
                                Intent().apply { putExtra(NinjaAddCardActivity.CARD_SENDED_SUCCESFULL, true) })
                    }
                })
    }

    fun navigateToRules(): Unit? = product?.infoOfSubscription?.let { Utils.goToUrl(App.getContext(), it.url) }

    private fun sendPurchaseRequest(email: String, token: String, productType: String) {
        mPurchaseRequestSubscription = getPurchaseRequest(email, token)
                .applySchedulers()
                .subscribe(shortSubscription {
                    mFeedNavigator?.showPurchaseSuccessfullFragment(productType)
                })
    }

    private fun getPurchaseRequest(email: String, token: String) =
            fromEmitter<SimpleResponse>({ emitter ->
                val sendRequest = PaymentNinjaPurchaseRequest(App.getContext(), token, email)
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
}