package com.topface.billing.ninja.fragments.three_d_secure

import android.app.Activity
import android.content.Intent
import android.databinding.ObservableBoolean
import android.databinding.ObservableField
import android.databinding.ObservableInt
import android.graphics.Paint
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import com.topface.billing.ninja.IFinishDelegate
import com.topface.billing.ninja.NinjaAddCardActivity
import com.topface.billing.ninja.PurchaseError
import com.topface.topface.utils.Utils

/**
 * Вью-модель для 3ds валидации
 * Created by ppavlik on 28.04.17.
 */
class ThreeDSecureViewModel(private val mSettings: PurchaseError,
                            private var mFinishCallback: IFinishDelegate?) : IPaymentNinjaWebViewClient {

    companion object {
        private const val POST_DATA_TEMPLATE = "PaReq=%s&MD=%s&TermUrl=%s"
    }

    val layerType = ObservableInt(WebView.LAYER_TYPE_SOFTWARE)
    val paint = ObservableField<Paint>()
    val isJavaScriptEnabled = ObservableBoolean(true)
    val isVerticalFadingEdgeEnabled = ObservableBoolean(true)
    val postUrl = ObservableField(mSettings.settings.acsUrl)
    val postData = ObservableField(String.format(POST_DATA_TEMPLATE, mSettings.settings.PaReq, mSettings.settings.MD, mSettings.settings.termUrl))
    val client = PaymentNinjaWebViewClient(this)

    override fun onPageStarted(url: String) {
        checkLink(mSettings.settings.paymentSuccessUrl, mSettings.settings.paymentFailUrl, url)
    }

    override fun onPageFinished(url: String) {
        checkLink(mSettings.settings.paymentSuccessUrl, mSettings.settings.paymentFailUrl, url)
    }

    override fun onReceivedError(request: WebResourceRequest?, error: WebResourceError?) {
        finishWithFail()
    }

    private fun checkLink(paymentSuccessUrl: String, paymentFailUrl: String, currentLink: String) =
            when (currentLink) {
                paymentSuccessUrl -> finishWithSuccess()
                paymentFailUrl -> finishWithSuccess()
                else -> Unit
            }

    private fun finishWithFail() {
        Utils.showErrorMessage()
        mFinishCallback?.finishWithResult(Activity.RESULT_OK, Intent().apply { putExtra(NinjaAddCardActivity.CARD_SENDED_SUCCESFULL, true) })
    }

    private fun finishWithSuccess() {
        mFinishCallback?.finishWithResult(Activity.RESULT_OK, Intent().apply {
            putExtra(NinjaAddCardActivity.PURCHASE_SUCCESFULL, true)
            putExtra(NinjaAddCardActivity.PRODUCT, mSettings.product)
        })
    }

    fun release() {
        mFinishCallback = null
    }
}