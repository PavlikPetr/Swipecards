package com.topface.topface.ui.fragments.buy

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import com.topface.topface.App
import org.onepf.oms.appstore.googleUtils.Purchase

class GpMarketFragment : GoogleMarketBuyingFragment() {

    companion object {
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

    override fun onCreate(savedInstanceState: Bundle?) {
        with(arguments) {
            mSkuId = getString(GpPurchaseActivity.SKU_ID)
            mFrom = getString(GpPurchaseActivity.FROM)
            mIsSubscription = getBoolean(GpPurchaseActivity.IS_SUBSCRIPTION, false)
        }
        super.onCreate(savedInstanceState)

    }

    override fun onInAppBillingSupported() {
    }

    override fun onInAppBillingUnsupported() {
    }

    override fun onOpenIabSetupFinished(normaly: Boolean) {
        super.onOpenIabSetupFinished(normaly)
        if (isTestPurchasesAvailable) {
            setTestPaymentsState(App.getUserConfig().testPaymentFlag)
        }
        if (!TextUtils.isEmpty(mSkuId)) {
            buyNow(mSkuId, mIsSubscription)
        }
    }

    fun buyNow(id: String?, isSubscription: Boolean) {
        id?.let {
            if (isSubscription && !isTestPurchasesEnabled) {
                buySubscription(it)
            } else {
                buyItem(it)
            }
        }
    }

    interface onPurchaseActions {

        fun onPurchaseSuccess(product: Purchase)

        fun onPopupClosed()
    }

    fun setOnPurchaseActions(actions: onPurchaseActions) {
        mPurchaseActions = actions
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        isNeedCloseFragment = true
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onResume() {
        super.onResume()
        //Устанавливаем тестовые покупки
        if (isTestPurchasesAvailable) {
            setTestPaymentsState(App.getUserConfig().testPaymentFlag)
        }
        if (isNeedCloseFragment && isAdded) {
            activity.supportFragmentManager.beginTransaction().remove(this).commit()
        }
        mPurchaseActions?.onPopupClosed()
    }

    override fun onPurchased(product: Purchase) {
        super.onPurchased(product)
        mPurchaseActions?.onPurchaseSuccess(product)
    }

}