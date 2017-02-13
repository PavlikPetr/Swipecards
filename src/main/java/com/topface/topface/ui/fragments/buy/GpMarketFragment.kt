package com.topface.topface.ui.fragments.buy

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.topface.topface.App
import com.topface.topface.utils.rx.safeUnsubscribe
import com.topface.topface.utils.rx.shortSubscription
import org.onepf.oms.appstore.googleUtils.Purchase
import rx.Observable
import rx.Subscription
import java.util.concurrent.TimeUnit

class GpMarketFragment : GoogleMarketBuyingFragment() {

    companion object {
        const val PURCHASE_TIMEOUT = 500L
        fun newInstance(skuId: String, isSubscription: Boolean, from: String) = GpMarketFragment().apply {
            arguments = Bundle().apply {
                putString(GpPurchaseActivity.SKU_ID, skuId)
                putString(GpPurchaseActivity.FROM, from)
                putBoolean(GpPurchaseActivity.IS_SUBSCRIPTION, isSubscription)
            }
        }
    }

    private var mSkuId: String? = null
    private var mFrom: String? = null
    private var mIsSubscription = false
    private var isNeedCloseFragment = false
    private var mPurchaseActions: onPurchaseActions? = null
    private var mTimerSubscription: Subscription? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        retainInstance = true
        with(arguments) {
            mSkuId = getString(GpPurchaseActivity.SKU_ID)
            mFrom = getString(GpPurchaseActivity.FROM)
            mIsSubscription = getBoolean(GpPurchaseActivity.IS_SUBSCRIPTION, false)
        }
        super.onCreate(savedInstanceState)
    }

    override fun onInAppBillingSupported() {}

    override fun onInAppBillingUnsupported() {
        mPurchaseActions?.onPopupClosed()
    }

    override fun onOpenIabSetupFinished(normaly: Boolean) {
        super.onOpenIabSetupFinished(normaly)
        if (isTestPurchasesAvailable) {
            setTestPaymentsState(App.getUserConfig().testPaymentFlag)
        }
        buyNow()
    }

    fun buyNow() {
        mSkuId?.let {
            if (mIsSubscription && !isTestPurchasesEnabled) {
                buySubscription(it)
            } else {
                buyItem(it)
            }
        }
    }

    interface onPurchaseActions {

        fun onPurchaseSuccess(product: Purchase?)

        fun onPopupClosed()
    }

    fun setOnPurchaseActions(actions: onPurchaseActions) {
        mPurchaseActions = actions
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == BUYING_REQUEST) {
            if (resultCode == Activity.RESULT_CANCELED) {
                isNeedCloseFragment = true
            } else if (resultCode == Activity.RESULT_OK) {
                /*
                bypass про запас
                ждем срабатывания колбека покупки, но на всякий случай запустим таймер, который
                закроет экран покупок
                 */
                mTimerSubscription = Observable.timer(PURCHASE_TIMEOUT, TimeUnit.MILLISECONDS)
                        .subscribe(shortSubscription {
                            mPurchaseActions?.onPurchaseSuccess(null)
                        })
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onResume() {
        super.onResume()
        //Устанавливаем тестовые покупки
        if (isTestPurchasesAvailable) {
            setTestPaymentsState(App.getUserConfig().testPaymentFlag)
        }
        if (isNeedCloseFragment && isAdded) {
            mPurchaseActions?.onPopupClosed()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mTimerSubscription.safeUnsubscribe()
    }

    override fun onPurchased(product: Purchase) {
        super.onPurchased(product)
        mPurchaseActions?.onPurchaseSuccess(product)
    }

}