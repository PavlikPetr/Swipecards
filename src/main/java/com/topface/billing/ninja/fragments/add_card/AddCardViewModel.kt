package com.topface.billing.ninja.fragments.add_card

import android.app.Activity
import android.content.Intent
import android.databinding.Observable
import android.databinding.ObservableBoolean
import android.databinding.ObservableField
import android.databinding.ObservableInt
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import com.topface.billing.ninja.*
import com.topface.billing.ninja.CardUtils.UtilsForCard
import com.topface.billing.ninja.CardUtils.UtilsForCard.EMAIL_ADDRESS
import com.topface.billing.ninja.CardUtils.UtilsForCard.INPUT_DELAY
import com.topface.billing.ninja.fragments.add_card.CardType.Companion.CVV_DEFAULT
import com.topface.framework.JsonUtils
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.data.Products
import com.topface.topface.requests.PaymentNinjaPurchaseRequest
import com.topface.topface.ui.fragments.buy.pn_purchase.PaymentNinjaProduct
import com.topface.topface.ui.fragments.feed.feed_base.IFeedNavigator
import com.topface.topface.utils.Utils
import com.topface.topface.utils.extensions.getPurchaseScreenTitle
import com.topface.topface.utils.extensions.getRequestSubscriber
import com.topface.topface.utils.extensions.getString
import com.topface.topface.utils.rx.RxFieldObservable
import com.topface.topface.utils.rx.applySchedulers
import com.topface.topface.utils.rx.safeUnsubscribe
import com.topface.topface.utils.rx.shortSubscription
import rx.Subscription
import rx.subscriptions.CompositeSubscription
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates

/**
 * ВьюМодель добавления карт
 */

class AddCardViewModel(private val data: Bundle, private val mNavigator: IFeedNavigator,
                       private val mFinishCallback: IFinishDelegate) {

    val numberText = RxFieldObservable<String>()
    val numberMaxLength = ObservableInt(19)
    val cardIcon = ObservableInt()
    val numberError = ObservableField<String>()
    val numberFocus = ObservableBoolean()

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
    val cvvMaxLength = ObservableInt(CVV_DEFAULT)
    val cvvError = ObservableField<String>()

    val trhuText = RxFieldObservable<String>()
    val trhuError = ObservableField<String>()

    val emailChangedCallback = object : Observable.OnPropertyChangedCallback() {
        override fun onPropertyChanged(observable: Observable?, p1: Int) = (observable as? ObservableField<*>)?.let {
            (it.get() as? String)?.let {
                if (it.length > 2) {
                    validateEmail()
                }
            }
        } ?: Unit
    }

    val emailText = ObservableField<String>()
    val emailError = ObservableField<String>()
    val cardFieldsSubscription = CompositeSubscription()
    val productTitle = ObservableField<String>()
    val isAutoPayDescriptionVisible = ObservableBoolean(false)
    val isAutoPayEnabled = ObservableBoolean(true)
    val isEmailFormNeeded = ObservableBoolean(false)
    val firstDescriptionText = ObservableField<String>()
    val secondDescriptionText = ObservableField<String>()
    val isFirstDescriptionVisible = ObservableBoolean(false)
    val isSecondDescriptionVisible = ObservableBoolean(false)
    val progressVisibility = ObservableBoolean(false)
    val buttonTextVisibility = ObservableInt(View.VISIBLE)
    val buttonText = ObservableField<String>(R.string.general_add.getString())
    val isButtonEnabled = ObservableBoolean(false)
    val isInputEnabled = ObservableBoolean(true)
    val titleVisibility = ObservableInt(View.GONE)

    private var mIsProgressVisible by Delegates.observable(false) { _, _, newValue ->
        progressVisibility.set(newValue)
        buttonTextVisibility.set(if (newValue) View.GONE else View.VISIBLE)
    }

    val numberWatcher = NumberWatcher()
    val trhuWatcher = TrhuWatcher()

    private val mProduct: PaymentNinjaProduct? = data.getParcelable(NinjaAddCardActivity.EXTRA_BUY_PRODUCT)
    private val mIsTestPurchase = data.getBoolean(NinjaAddCardActivity.EXTRA_IS_TEST_PURCHASE, false)
    private val mIs3DSPurchase = data.getBoolean(NinjaAddCardActivity.EXTRA_IS_3DS_PURCHASE, false)
    private val mSource: String? = data.getString(NinjaAddCardActivity.EXTRA_SOURCE)
    private var m3DSSwitchSubscription: Subscription? = null

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
        mProduct?.let {
            productTitle.set(it.getPurchaseScreenTitle())
            titleVisibility.set(View.VISIBLE)
            buttonText.set(R.string.ninja_button_buy.getString())

            if (it.type == Products.ProductType.COINS.getName() && it.isAutoRefilled) {
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
                .throttleLast(INPUT_DELAY, TimeUnit.MILLISECONDS)
                .subscribe(shortSubscription {
                    if ((it as String).isEmpty()) {
                        cardIcon.set(0)
                    }
                    if (it.length >= 4) {
                        setTemplate(giveMeBrand(it, UtilsForCard.cardBrands))
                    }
                    if (it.length == numberMaxLength.get()) {
                        validateNumber()
                    }
                })
        )

        cardFieldsSubscription.add(trhuText.filedObservable
                .filter { it.length >= 2 }
                .distinctUntilChanged()
                .throttleLast(INPUT_DELAY, TimeUnit.MILLISECONDS)
                .subscribe(shortSubscription {
                    it?.let {
                        trhuText.set(it)
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
                ?.let { cardBrands[it] }
                ?: CardType.DEFAULT
    }

    fun release() {
        cardFieldsSubscription.clear()
        cvvText.removeOnPropertyChangedCallback(cvvChangedCallback)
        emailText.removeOnPropertyChangedCallback(emailChangedCallback)
        mPurchaseRequestSubscription.safeUnsubscribe()
        m3DSSwitchSubscription.safeUnsubscribe()
    }

    fun onClick() {
        if (validateNumber() && validateCvv() && validateTrhu() && validateEmail()) {
            val trhuString = trhuText.get()
            var month: String = ""
            var year: String = ""
            if (!TextUtils.isEmpty(trhuString) && UtilsForCard.isValidTrhu(trhuString)) {
                month = trhuString.substring(0, 2)
                year = trhuString.substring(3)
            }
            val cardModel = AddCardModel(
                    App.get().options.paymentNinjaInfo.projectKey,
                    numberText.get()?.replace(UtilsForCard.SPACE_DIVIDER, "") ?: "",
                    month,
                    year,
                    cvvText.get() ?: "",
                    emailText.get() ?: ""
            )
            isInputEnabled.set(false)
            mIsProgressVisible = true
            AddCardRequest().getRequestObservable(App.get(), cardModel)
                    .applySchedulers()
                    .subscribe({
                        mProduct?.let {
                            sendPurchaseRequest(it.id, mSource ?: NinjaAddCardActivity.UNKNOWN_PLACE, it.type)
                        } ?: mFinishCallback.finishWithResult(Activity.RESULT_OK,
                                Intent().apply { putExtra(NinjaAddCardActivity.CARD_SENDED_SUCCESFULL, true) })
                    }, {
                        mNavigator.showPaymentNinjaErrorDialog(data.getBoolean(NinjaAddCardActivity.EXTRA_FROM_INSTANT_PURCHASE) ||
                                mProduct == null) {
                            if (isEmailFormNeeded.get()) {
                                emailText.set("")
                            }
                            numberText.set("")
                            cvvText.set("")
                            trhuText.set("")
                            numberFocus.set(true)
                        }
                        isInputEnabled.set(true)
                        isButtonEnabled.set(false)
                        mIsProgressVisible = false
                    })
        }
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

    private fun validateNumber(): Boolean {
        if (!numberText.get().isNullOrEmpty() &&
                UtilsForCard.isDigits(numberText.get().replace(UtilsForCard.SPACE_DIVIDER, "")) &&
                numberText.get().length <= numberMaxLength.get()) {
            // валидация по алгоритму Луна
            if (!UtilsForCard.luhnsAlgorithm(numberText.get().replace(UtilsForCard.SPACE_DIVIDER, ""))) {
                numberError.set(R.string.ninja_number_error.getString())
                readyCheck.put(numberText, false)
            } else {
                numberError.set(Utils.EMPTY)
                readyCheck.put(numberText, true)
            }
        } else {
            numberError.set(R.string.ninja_number_error.getString())
            readyCheck.put(numberText, false)
        }
        updateButton()
        return readyCheck[numberText] ?: false
    }

    private fun validateTrhu(): Boolean {
        val trhuString = trhuText.get()
        if (!trhuString.isNullOrEmpty() && trhuString.length == UtilsForCard.TRHU_LENGTH) {
            if (!UtilsForCard.isValidTrhu(trhuString)) {
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
        return readyCheck[trhuText] ?: false
    }

    private fun validateCvv(): Boolean {
        if (!cvvText.get().isNullOrEmpty() && cvvText.get().length == cvvMaxLength.get()) {
            if (!UtilsForCard.isDigits(cvvText.get())) {
                cvvError.set(R.string.ninja_cvv_error.getString())
                readyCheck.put(cvvText, false)
            } else {
                cvvError.set(Utils.EMPTY)
                readyCheck.put(cvvText, true)
            }
        } else {
            cvvError.set(R.string.ninja_cvv_error.getString())
            readyCheck.put(cvvText, false)
        }
        updateButton()
        return readyCheck[cvvText] ?: false
    }

    private fun validateEmail(): Boolean {
        if (!emailText.get().matches(EMAIL_ADDRESS.toRegex())) {
            emailError.set(R.string.ninja_email_error.getString())
            readyCheck.put(emailText, false)
        } else {
            emailError.set(Utils.EMPTY)
            readyCheck.put(emailText, true)
        }
        updateButton()
        return readyCheck[emailText] ?: false
    }

    fun navigateToRules(): Unit? = mProduct?.subscriptionInfo?.let { Utils.goToUrl(App.getContext(), it.url) }

    private fun sendPurchaseRequest(productId: String, source: String, productType: String) {
        mPurchaseRequestSubscription = PaymentNinjaPurchaseRequest(App.getContext(), productId, source,
                mIsTestPurchase, isAutoPayEnabled.get(), mIs3DSPurchase).getRequestSubscriber()
                .applySchedulers()
                .subscribe({
                    mNavigator.showPurchaseSuccessfullFragment(productType, Bundle()
                            .apply { putBoolean(NinjaAddCardActivity.CARD_SENDED_SUCCESFULL, true) })
                    mIsProgressVisible = false
                    isInputEnabled.set(true)
                },
                        { error ->
                            mProduct?.let {
                                handlePurchaseError(PurchaseError(JsonUtils.fromJson(error.message, ThreeDSecureParams::class.java), it))
                            }
                            mIsProgressVisible = false
                            isInputEnabled.set(true)
                        })
    }

    private fun handlePurchaseError(secureSettings: PurchaseError) {
        App.getAppComponent().eventBus().setData(secureSettings)
    }
}