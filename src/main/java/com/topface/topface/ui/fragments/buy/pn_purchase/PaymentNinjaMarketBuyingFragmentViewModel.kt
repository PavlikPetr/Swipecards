package com.topface.topface.ui.fragments.buy.pn_purchase

import android.app.Activity
import android.content.Intent
import android.databinding.ObservableBoolean
import android.databinding.ObservableField
import android.databinding.ObservableInt
import android.view.View
import com.topface.billing.ninja.NinjaAddCardActivity
import com.topface.billing.ninja.PurchaseError
import com.topface.billing.ninja.ThreeDSecureParams
import com.topface.framework.JsonUtils
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.data.Options
import com.topface.topface.data.Profile
import com.topface.topface.requests.PaymentNinjaPurchaseRequest
import com.topface.topface.requests.handlers.ErrorCodes
import com.topface.topface.ui.fragments.feed.feed_base.IFeedNavigator
import com.topface.topface.utils.CacheProfile
import com.topface.topface.utils.ILifeCycle
import com.topface.topface.utils.Utils
import com.topface.topface.utils.databinding.MultiObservableArrayList
import com.topface.topface.utils.extensions.*
import com.topface.topface.utils.rx.RxObservableField
import com.topface.topface.utils.rx.applySchedulers
import com.topface.topface.utils.rx.safeUnsubscribe
import com.topface.topface.utils.rx.shortSubscription
import rx.subscriptions.CompositeSubscription

/**
 * Buy buttons view model
 * Created by petrp on 02.03.2017.
 */
class PaymentNinjaMarketBuyingFragmentViewModel(private val mNavigator: IFeedNavigator,
                                                private val mIsVipPurchaseProducts: Boolean,
                                                private val mFrom: String) : ILifeCycle {
    companion object {
        const val AUTOREFILL_RULES_URL = "https://topface.com/en/autorefill/"
    }

    val isCheckBoxVisible = ObservableInt(View.GONE)
    val isChecked = RxObservableField(true)
    val cardInfo = ObservableField(Utils.EMPTY)
    val autofillVisibility = ObservableInt(View.GONE)
    val isAutoFillEnabled = ObservableBoolean(true)
    val progressVisibility = ObservableBoolean(false)
    val data = MultiObservableArrayList<Any>()
    private val mEventBus by lazy {
        App.getAppComponent().eventBus()
    }

    private val mAppState by lazy {
        App.getAppComponent().appState()
    }

    private var mIsTestPurchase = false
    private var mIs3DSAvailable = false
    private var mAutoFillUrl: String? = null
    private var mIsPremiumUser = App.get().profile.premium
    private var mPaymentNinjaInfo = App.get().options.paymentNinjaInfo

    private var mSubscriptions = CompositeSubscription()

    init {
        mSubscriptions.add(App.getAppComponent().appState().getObservable(Profile::class.java)
                .map { it.isEditor }
                .distinctUntilChanged()
                .applySchedulers()
                .subscribe(shortSubscription {
                    it?.let {
                        if (it) {
                            if (data.find { it is TestPurchaseSwitch } == null) {
                                // находим заголовок списка и после него добавляем переключатель тестовых покупок
                                // ну а если не нашли заголовок, то ставим в начало списка
                                with(data.indexOfFirst { it is BuyScreenTitle } + 1) {
                                    if (data.isEntry(this)) {
                                        data.addAll(this, arrayListOf(TestPurchaseSwitch(mIsTestPurchase), ThreeDSecurePurchaseSwitch(mIs3DSAvailable)))
                                    } else {
                                        data.addAll(arrayOf(TestPurchaseSwitch(mIsTestPurchase), ThreeDSecurePurchaseSwitch(mIs3DSAvailable)))
                                    }
                                }
                            } else Unit
                        } else {
                            data.remove(data.find { it is TestPurchaseSwitch })
                        }
                    }
                }))
        mSubscriptions.addAll(App.getAppComponent().appState().getObservable(Options::class.java)
                .map { it.paymentNinjaInfo }
                .distinctUntilChanged { t1, t2 -> t1 == t2 }
                .applySchedulers()
                .subscribe(shortSubscription {
                    it?.let {
                        mPaymentNinjaInfo = it
                        showCardInfoIfPosible()
                    }
                }))
        mSubscriptions.add(isChecked.asRx.distinctUntilChanged()
                .subscribe(shortSubscription { it?.let { initAutofillView(it) } }))
        mSubscriptions.add(mEventBus
                .getObservable(TestPurchaseSwitch::class.java)
                .distinctUntilChanged { (isChecked1), (isChecked2) -> isChecked1 == isChecked2 }
                .subscribe(shortSubscription {
                    it?.let { mIsTestPurchase = it.isChecked }
                }))
        mSubscriptions.add(mEventBus
                .getObservable(ThreeDSecurePurchaseSwitch::class.java)
                .distinctUntilChanged { (isChecked1), (isChecked2) -> isChecked1 == isChecked2 }
                .subscribe(shortSubscription {
                    it?.let { mIs3DSAvailable = it.isChecked }
                }))
        mSubscriptions.add(mAppState.getObservable(Profile::class.java)
                .map { it.premium }
                .distinctUntilChanged()
                .subscribe(shortSubscription {
                    mIsPremiumUser = it
                    showCardInfoIfPosible()
                    if (it && mIsVipPurchaseProducts) {
                        initStub()
                    } else {
                        initProducts()
                    }
                }))
        initAutofillView(isChecked.get() && isCheckBoxVisible.get() == View.VISIBLE)
    }

    private fun showCardInfoIfPosible() {
        if (!mPaymentNinjaInfo.isCradAvailable() || (mIsVipPurchaseProducts && mIsPremiumUser)) {
            isCheckBoxVisible.set(View.GONE)
        } else {
            cardInfo.set(String.format(R.string.use_card.getString(), mPaymentNinjaInfo.lastDigits))
            isCheckBoxVisible.set(View.VISIBLE)
            initAutofillView(isChecked.get())
        }
    }

    // генерим итемы для заглушки когда юзер уже имеет Вип
    private fun initStub() {
        data.replaceData(arrayListOf(InvisibleModeSwitch(false), BlackListItem()))
    }

    private fun initProducts() {
        data.replaceData(arrayListOf<Any>().apply {
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
        )
    }

    private fun initAutofillView(isCardChecked: Boolean) =
            if (isCardChecked) {
                data.find {
                    (it as? PaymentNinjaProduct)?.isAutoRefilled ?: false
                }?.let {
                    mAutoFillUrl = (it as? PaymentNinjaProduct)?.subscriptionInfo?.url
                    isAutoFillEnabled.set(true)
                    autofillVisibility.set(View.VISIBLE)
                } ?: autofillVisibility.set(View.GONE)
            } else autofillVisibility.set(View.GONE)

    fun buyProduct(product: PaymentNinjaProduct) {
        if (!App.get().options.paymentNinjaInfo.isCradAvailable() ||
                !isChecked.get()) {
            mNavigator.showPaymentNinjaAddCardScreen(product, mFrom, mIsTestPurchase, mIs3DSAvailable)
        } else {
            progressVisibility.set(true)
            mSubscriptions.add(PaymentNinjaPurchaseRequest(App.getContext(), product.id, mFrom,
                    mIsTestPurchase, isAutoFillEnabled.get(), mIs3DSAvailable)
                    .getRequestSubscriber()
                    .applySchedulers()
                    .subscribe({
                        mNavigator.showPurchaseSuccessfullFragment(product.type)
                        progressVisibility.set(false)
                    }, {
                        it?.let {
                            progressVisibility.set(false)
                            val error = JsonUtils.fromJson(it.message, ThreeDSecureParams::class.java)
                            if (error.errorCode == ErrorCodes.PAYMENT_NINJA_3DSECURE_ERROR) {
                                mNavigator.showPaymentNinja3DS(PurchaseError(error, product))
                            } else {
                                Utils.showErrorMessage()
                            }
                        }
                    }))
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == NinjaAddCardActivity.REQUEST_CODE && resultCode == Activity.RESULT_OK &&
                data != null && data.getBooleanExtra(NinjaAddCardActivity.PURCHASE_SUCCESFULL, false) &&
                data.hasExtra(NinjaAddCardActivity.PRODUCT)) {
            mNavigator.showPurchaseSuccessfullFragment(data.getParcelableExtra<PaymentNinjaProduct>(NinjaAddCardActivity.PRODUCT).type)
        }
    }

    fun onLinkClick() = mNavigator.openUrl(mAutoFillUrl?.takeIf(String::isNotEmpty) ?: AUTOREFILL_RULES_URL)

    fun release() {
        mSubscriptions.safeUnsubscribe()
    }
}