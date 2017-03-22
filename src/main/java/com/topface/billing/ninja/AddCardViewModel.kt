package com.topface.billing.ninja

import android.app.Activity
import android.content.Intent
import android.databinding.Observable
import android.databinding.ObservableBoolean
import android.databinding.ObservableField
import android.databinding.ObservableInt
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import com.topface.billing.ninja.CardUtils.UtilsForCard
import com.topface.billing.ninja.CardUtils.UtilsForCard.EMAIL_ADDRESS
import com.topface.billing.ninja.CardUtils.UtilsForCard.INPUT_DELAY
import com.topface.framework.utils.Debug
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.data.Products
import com.topface.topface.requests.IApiResponse
import com.topface.topface.requests.PaymentNinjaPurchaseRequest
import com.topface.topface.ui.fragments.buy.pn_purchase.PaymentNinjaProduct
import com.topface.topface.ui.fragments.feed.feed_base.FeedNavigator
import com.topface.topface.utils.Utils
import com.topface.topface.utils.extensions.getRequestSubscriber
import com.topface.topface.utils.extensions.getString
import com.topface.topface.utils.rx.*
import rx.Subscription
import rx.subscriptions.CompositeSubscription
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * ВьюМодель добавления карт
 */

class AddCardViewModel(private val data: Bundle, private val mNavigator: FeedNavigator, private val mFinishCallback: IFinishDelegate) {

    val numberText = RxFieldObservable<String>()
    val numberMaxLength = ObservableInt(19)
    val cardIcon = ObservableInt()
    val numberError = ObservableField<String>()

    private var mPurchaseRequestSubscription: Subscription? = null

    val cvvChangedCallback = object : Observable.OnPropertyChangedCallback() {
        override fun onPropertyChanged(observable: Observable?, p1: Int) = (observable as? ObservableField<*>)?.let {
            (it.get() as? String)?.let {
                if (it.length == cvvMaxLength.get()) {
                    validateCvv()
                }
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
        override fun onPropertyChanged(observable: Observable?, p1: Int) = (observable as? ObservableField<*>)?.let {
            (it.get() as? String)?.let {
                if (it.length >= 6) {
                    if (!it.matches(EMAIL_ADDRESS.toRegex())) {
                        Debug.error("--------------------EMAIL невалидный-----------------------------")
                        emailError.set(R.string.ninja_email_error.getString())
                        readyCheck.put(emailText, false)
                    } else {
                        emailError.set(Utils.EMPTY)
                        readyCheck.put(emailText, true)
                    }
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
    val isEmailFormNeeded = ObservableBoolean(false)
    val firstDescriptionText = ObservableField<String>()
    val secondDescriptionText = ObservableField<String>()
    val isFirstDescriptionVisible = ObservableBoolean(false)
    val isSecondDescriptionVisible = ObservableBoolean(false)

    val isButtonEnabled = ObservableBoolean(false)
    val isInputEnabled = ObservableBoolean(true)
    val titleVisibility = ObservableInt(View.GONE)

    val product: PaymentNinjaProduct? = data.getParcelable(NinjaAddCardActivity.EXTRA_BUY_PRODUCT)

    private val mSource: String? = data.getString(NinjaAddCardActivity.EXTRA_SOURCE)

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

            if (it.type == Products.ProductType.COINS.getName() && it.typeOfSubscription == 1) {
                isAutoPayDescriptionVisible.set(true)
                firstDescriptionText.set(it.subscriptionInfo.text)
                isFirstDescriptionVisible.set(true)
                secondDescriptionText.set(R.string.ninja_text_4.getString())
                isSecondDescriptionVisible.set(true)
            } else if (it.type == Products.ProductType.PREMIUM.getName()) {
                if (it.trialPeriod > 0) {
                    // trial vip
                    isFirstDescriptionVisible.set(true)
                    isSecondDescriptionVisible.set(true)
                    firstDescriptionText.set(R.string.ninja_text_trial_1.getString())
                    val days = Utils.getQuantityString(R.plurals.ninja_trial_days, it.trialPeriod, it.trialPeriod)
                    secondDescriptionText.set(String.format(R.string.ninja_text_trial_2.getString(), days, it.price, it.currencyCode))
                } else {
                    // vip
                    isFirstDescriptionVisible.set(true)
                    firstDescriptionText.set(R.string.ninja_text_5.getString())
                }
            }
        }
        isEmailFormNeeded.set(TextUtils.isEmpty(email))
        emailText.addOnPropertyChangedCallback(emailChangedCallback)
        cvvText.addOnPropertyChangedCallback(cvvChangedCallback)

        if (!isEmailDefined) {
            emailText.set(email)
        }

        cardFieldsSubscription.add(numberText.filedObservable
                .distinctUntilChanged()
                .map { str -> UtilsForCard.formattingForCardNumber(str) }
                .throttleLast(INPUT_DELAY, TimeUnit.MILLISECONDS)
                .subscribe(shortSubscription {
                    if (it.isEmpty()) {
                        cardIcon.set(0)
                    }
                    if (it.length == 4) {
                        setTemplate(giveMeBrand(it, UtilsForCard.cardBrands))
                    }
                    if (it.length == numberMaxLength.get()) {
                        validateNumber()
                    }
                    numberText.set(it)
                })
        )

        cardFieldsSubscription.add(trhuText.filedObservable
                .filter { it.length >= 2 }
                .distinctUntilChanged()
                .map { str -> UtilsForCard.setTrhuDivider(str) }
                .throttleLast(INPUT_DELAY, TimeUnit.MILLISECONDS)
                .subscribe(shortSubscription {
                    it?.let {
                        setTRHU(it)
                        if (it.length == UtilsForCard.TRHU_LENGTH) {
                            validateTrhu()
                        }
                    }
                })
        )
    }

    private fun updateButton() = isButtonEnabled.set(!readyCheck.containsValue(false))

    private fun setTemplate(cardType: CardType) {
        numberMaxLength.set(cardType.numberMaxLength)
        cvvMaxLength.set(cardType.cvvMaxLength)
        cardIcon.set(cardType.cardIcon)
    }

    private fun giveMeBrand(cardNumber: String, cardBrands: HashMap<Regex, CardType>): CardType {
        return cardBrands.keys
                .find { cardNumber.matches(it) }
                ?.let { cardBrands.getOrDefault(it, CardType.DEFAULT) }
                ?: CardType.DEFAULT
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
                        mNavigator.showPaymentNinjaErrorDialog(data.getBoolean(NinjaAddCardActivity.EXTRA_FROM_INSTANT_PURCHASE) ||
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
                            mNavigator.showPurchaseSuccessfullFragment(it.type)
                            //sendPurchaseRequest(it.id, mSource ?: NinjaAddCardActivity.UNKNOWN_PLACE, it.type)
                        } ?: mFinishCallback.finishWithResult(Activity.RESULT_OK,
                                Intent().apply { putExtra(NinjaAddCardActivity.CARD_SENDED_SUCCESFULL, true) })
                    }
                })
    }

    fun onNumberChange(v: View, hasFocus: Boolean) {
        if (!hasFocus) {
            validateNumber()
        }
    }

    fun onTrhuChange(v: View, hasFocus: Boolean) {
        if (!hasFocus) {
            validateTrhu()
        }
    }

    fun onCvvChange(v: View, hasFocus: Boolean) {
        if (!hasFocus) {
            validateCvv()
        }
    }

    private fun validateNumber() {
        if (!numberText.get().isNullOrEmpty() && UtilsForCard.isDigits(numberText.get().replace(UtilsForCard.SPACE_DIVIDER, ""))) {
            // валидация по алгоритму Луна
            if (!UtilsForCard.luhnsAlgorithm(numberText.get().replace(UtilsForCard.SPACE_DIVIDER, ""))) {
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

    private fun validateTrhu() {
        val trhuString = trhuText.get()
        if (!trhuString.isNullOrEmpty() && trhuString.length == UtilsForCard.TRHU_LENGTH) {
            if (!UtilsForCard.isValidTrhu(trhuString)) {
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

    private fun validateCvv() {
        if (!cvvText.get().isNullOrEmpty()) {
            if (cvvText.get().length < cvvMaxLength.get() || !UtilsForCard.isDigits(cvvText.get())) {
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

    fun navigateToRules(): Unit? = product?.subscriptionInfo?.let { Utils.goToUrl(App.getContext(), it.url) }

    private fun sendPurchaseRequest(productId: String, source: String, productType: String) {
        mPurchaseRequestSubscription = PaymentNinjaPurchaseRequest(App.getContext(), productId, source).getRequestSubscriber()
                .applySchedulers()
                .subscribe(shortSubscription {
                    mNavigator.showPurchaseSuccessfullFragment(productType)
                })
    }
}