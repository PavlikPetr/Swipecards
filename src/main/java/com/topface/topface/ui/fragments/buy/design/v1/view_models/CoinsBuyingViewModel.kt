package com.topface.topface.ui.fragments.buy.design.v1.view_models

import android.os.Bundle
import com.topface.billing.IBilling
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.data.BuyButtonData
import com.topface.topface.data.Products
import com.topface.topface.data.Profile
import com.topface.topface.ui.fragments.buy.design.v1.*
import com.topface.topface.ui.fragments.feed.enhanced.base.BaseViewModel
import com.topface.topface.utils.databinding.MultiObservableArrayList
import com.topface.topface.utils.extensions.getCoinsProducts
import com.topface.topface.utils.extensions.getLikesProducts
import com.topface.topface.utils.rx.safeUnsubscribe
import com.topface.topface.utils.rx.shortSubscription
import org.onepf.oms.appstore.googleUtils.Purchase
import rx.Subscription

/**
 * Created by ppavlik on 08.06.17.
 * Вью модель экрана покупок likes && coins с GP
 */
class CoinsBuyingViewModel(private val mBundle: Bundle, private val mProducts: Products) : IBilling, BaseViewModel() {

    companion object {
        private const val UNDEFINED = 0
        private const val IN_APP_BILLING_SUPPORTED = 1
        private const val IN_APP_BILLING_UNSUPPORTED = 2
    }

    private val mFrom by lazy {
        mBundle.getString(CoinsBuyingFragment.FROM)
    }

    private val mAppState by lazy {
        App.getAppComponent().appState()
    }

    private val mUserConfig by lazy {
        App.getUserConfig()
    }

    var mEditorSubscription: Subscription? = null

    val data = MultiObservableArrayList<Any>()
    var mIsEditor = App.get().profile.isEditor
    var mCurrentItemsType = CoinsBuyingViewModel.UNDEFINED

    init {
        mEditorSubscription = mAppState.getObservable(Profile::class.java)
                .map { it.isEditor }
                .distinctUntilChanged()
                .subscribe(shortSubscription {
                    mIsEditor = it
                    if (it) {
                        // пользователь стал редактором
                        // добавляем переключатель тестовых покупок только если на экране отображаются продукты
                        if (mCurrentItemsType == IN_APP_BILLING_SUPPORTED) {
                            removeItem({ it is TestPurchaseSwitchItem }, { it.add(0, TestPurchaseSwitchItem(mUserConfig.testPaymentFlag)) })
                        }
                    } else {
                        // если вдруг пользователь перестал быть редактором, то выпиливаем
                        // переключатель тестовых покупок
                        removeItem(predicate = { it is TestPurchaseSwitchItem })
                    }
                })
        showInAppBillingSupported()
    }

    private fun removeItem(predicate: (Any) -> Boolean = { false }, doAfterDelete: (ArrayList<Any>) -> Unit = {}) {
        val list = data.getList()
        list.removeAll(list.filter { predicate(it) })
        doAfterDelete(list)
        data.replaceData(list)
    }

    private fun showInAppBillingUnsupported() {
        mCurrentItemsType = CoinsBuyingViewModel.IN_APP_BILLING_UNSUPPORTED
        data.replaceData(arrayListOf<Any>(InAppBillingUnsupported()))
    }

    private fun showInAppBillingSupported() {
        mCurrentItemsType = CoinsBuyingViewModel.IN_APP_BILLING_SUPPORTED
        arrayListOf<Any>().apply {
            mProducts.getLikesProducts()
                    .filter { it.displayOnBuyScreen }
                    .forEach { add(LikeItem(it, mFrom)) }
            mProducts.getCoinsProducts()
                    .filter { it.displayOnBuyScreen }
                    .run {
                        val sorted = this.sortedBy { it.amount }
                        forEach { add(CoinItem(it, mFrom, it.getCoinsImg(sorted))) }
                    }
        }.run {
            if (size > 0) {
                data.replaceData(this.apply {
                    if (mIsEditor) {
                        add(0, TestPurchaseSwitchItem(mUserConfig.testPaymentFlag))
                    }
                })
            } else {
                showInAppBillingUnsupported()
            }
        }

    }

    private fun BuyButtonData.getCoinsImg(sortedList: List<BuyButtonData>) =
            when (sortedList.indexOfFirst { it == this }) {
                0 -> R.drawable.ic_purchase_coins_1
                1 -> R.drawable.ic_purchase_coins_2
                2 -> R.drawable.ic_purchase_coins_3
                else -> R.drawable.ic_purchase_coins_4
            }

    override fun onPurchased(product: Purchase) {
    }

    override fun onSubscriptionSupported() {
    }

    override fun onSubscriptionUnsupported() {
    }

    override fun onInAppBillingSupported() {
        showInAppBillingSupported()
    }

    override fun onInAppBillingUnsupported() {
        showInAppBillingUnsupported()
    }

    override fun release() {
        mEditorSubscription.safeUnsubscribe()
    }
}