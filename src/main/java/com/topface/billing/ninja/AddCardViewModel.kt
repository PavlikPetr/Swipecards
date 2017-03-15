package com.topface.billing.ninja

import android.databinding.ObservableBoolean
import android.databinding.ObservableField
import android.databinding.ObservableInt
import android.os.Bundle
import android.text.TextUtils
import com.topface.billing.ninja.CardUtils.UtilsForCard
import com.topface.billing.ninja.CardUtils.UtilsForCard.EMAIL_ADDRESS
import com.topface.billing.ninja.CardUtils.UtilsForCard.INPUT_DELAY
import com.topface.framework.utils.Debug
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.requests.IApiResponse
import com.topface.topface.ui.fragments.buy.pn_purchase.PaymentNinjaProduct
import com.topface.topface.ui.fragments.feed.feed_base.FeedNavigator
import com.topface.topface.utils.Utils
import com.topface.topface.utils.extensions.getString
import com.topface.topface.utils.rx.RxFieldObservable
import com.topface.topface.utils.rx.applySchedulers
import com.topface.topface.utils.rx.shortSubscription
import rx.Subscriber
import rx.subscriptions.CompositeSubscription
import java.util.*
import java.util.concurrent.TimeUnit


/**
 * ВьюМодель добавления карт
 */

class AddCardViewModel(val data:Bundle) {

    val numberText = object:RxFieldObservable<String>() {
        override fun set(value: String?) {
            super.set(value)
            if (!get().isNullOrEmpty() && UtilsForCard.isDigits(get().replace(UtilsForCard.SPACE_DIVIDER, ""))) {
                // валидация по алгоритму Луна
                if (!UtilsForCard.luhnsAlgorithm(get().replace(UtilsForCard.SPACE_DIVIDER, ""))) {
                    Debug.error("---------------------ОШИБКА ввода номера карты--------------------------------")
                    numberError.set(R.string.ninja_number_error.getString())
                    readyCheck.put(this, false)
                } else {
                    numberError.set(Utils.EMPTY)
                    readyCheck.put(this, true)
                }
            } else {
                Debug.error("---------------------ОШИБКА заполните поле ввода номера карты -----------------------")
                numberError.set(R.string.ninja_number_error.getString())
                readyCheck.put(this, false)
            }
            updateButton()
        }
    }
    val numberCursorPosition = ObservableInt()
    val numberMaxLength = ObservableInt(19)
    val cardIcon = ObservableInt()
    val numberError = ObservableField<String>()

    val cvvText = object: RxFieldObservable<String>() {
        override fun set(value: String?) {
            super.set(value)
            if (!get().isNullOrEmpty()) {
                if (get().length < cvvMaxLength.get() || !UtilsForCard.isDigits(get())) {
                    Debug.error("--------------------Все очень плохо-----слишком мало букав---или введен текст-------------------")
                    cvvError.set(R.string.ninja_cvv_error.getString())
                    readyCheck.put(this, false)
                } else {
                    cvvError.set(Utils.EMPTY)
                    readyCheck.put(this, true)
                }
            } else {
                Debug.error("---------------------ОШИБКА заполните поле cvv -----------------------")
                cvvError.set(R.string.ninja_cvv_error.getString())
                readyCheck.put(this, false)
            }
            updateButton()
        }
    }
    val cvvMaxLength = ObservableInt(3)
    val cvvError = ObservableField<String>()

    val trhuText = object: RxFieldObservable<String>() {
        override fun set(value: String?) {
            super.set(value)
            val trhuString = get()
            if (!trhuString.isNullOrEmpty() && trhuString.length == UtilsForCard.TRHU_LENGTH) {
                if (!UtilsForCard.isValidTrhu(trhuString)) {
                    Debug.error("---------------------ОШИБКА ввода Срока годности карты------------------------")
                    trhuError.set(R.string.ninja_trhu_error.getString())
                    readyCheck.put(this, false)
                } else {
                    trhuError.set(Utils.EMPTY)
                    readyCheck.put(this, true)
                }
            } else {
                trhuError.set(R.string.ninja_trhu_error.getString())
                readyCheck.put(this, false)
            }
            updateButton()
        }
    }
    val trhuCursorPosition = ObservableInt()
    val trhuError = ObservableField<String>()

    val emailText = object : RxFieldObservable<String>() {
        override fun set(value: String?) {
            super.set(value)
            if (!get().isNullOrEmpty()) {
                if (!get().matches(EMAIL_ADDRESS.toRegex())) {
                    Debug.error("--------------------EMAIL невалидный-----------------------------")
                    emailError.set(R.string.ninja_email_error.getString())
                    readyCheck.put(this, false)
                } else {
                    emailError.set(Utils.EMPTY)
                    readyCheck.put(this, true)
                }
            } else {
                Debug.error("---------------------ОШИБКА заполните поле email -----------------------")
                emailError.set(R.string.ninja_email_error.getString())
                readyCheck.put(this, false)
            }
            updateButton()
        }
    }
    val emailError = ObservableField<String>()

    val cardFieldsSubscription = CompositeSubscription()

    val productTitle = ObservableField<String>()

    val isAutoPayEnabled = ObservableBoolean()
    val isEmailFormNeeded = ObservableBoolean()

    val isButtonEnabled = ObservableBoolean(false)
    val isInputEnabled = ObservableBoolean(true)
    val isTitleHidden = ObservableBoolean(data.getBoolean(NinjaAddCardActivity.EXTRA_HIDE_TITLE, false))

    var mFeedNavigator : FeedNavigator? = null

    val product: PaymentNinjaProduct? = data.getParcelable(NinjaAddCardActivity.EXTRA_BUY_PRODUCT)

    private val readyCheck: MutableMap<Any, Boolean> = mutableMapOf()

    init {
        val isNeedEmail = TextUtils.isEmpty(App.getSessionConfig().socialAccountEmail)
        readyCheck.apply {
            put(numberText, false)
            put(cvvText, false)
            put(trhuText, false)
            put(emailText, !isNeedEmail)
        }
        product?.let {
            productTitle.set(it.titleTemplate)
        }
        isEmailFormNeeded.set(isNeedEmail)

        cardFieldsSubscription.add(numberText.filedObservable
                .filter { it.length >= 4 }
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
        numberCursorPosition.set(number.length)
    }

    fun setTRHU(trhu: String) {
        trhuText.set(trhu)
        trhuCursorPosition.set(trhu.length)
    }

    fun release() = cardFieldsSubscription.clear()

    fun onClick() {
        val trhuString = trhuText.get()
        var month: String  = ""
        var year: String = ""
        if (!TextUtils.isEmpty(trhuString) && UtilsForCard.isValidTrhu(trhuString)) {
            month = trhuString.substring(0, 2)
            year = trhuString.substring(3)
        }
        val email = if (isEmailFormNeeded.get()) {
            emailText.get()
        } else {
            App.getSessionConfig().socialAccountEmail
        }
        val cardModel = AddCardModel(
                App.get().options.paymentNinjaInfo.publicKey,
                numberText.get()?.replace(UtilsForCard.SPACE_DIVIDER, "") ?: "",
                month,
                year,
                cvvText.get() ?: "",
                email ?: ""
        )
        isInputEnabled.set(false)
        AddCardRequest().getRequestObservable(App.get(), cardModel)
                .applySchedulers()
                .subscribe(object : Subscriber<IApiResponse>() {
                    override fun onCompleted() {
                        isInputEnabled.set(true)
                    }

                    override fun onError(e: Throwable?) {
                        mFeedNavigator?.showPaymentNinjaErrorDialog(data.getBoolean(NinjaAddCardActivity.EXTRA_FROM_INSTANT_PURCHASE)) {
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
                        product?.let {
                            mFeedNavigator?.showPurchaseSuccessfullFragment(it.type)
                        }
                    }
        })
    }

}