package com.topface.billing.ninja.fragments

import android.annotation.TargetApi
import android.app.Activity
import android.content.Intent
import android.databinding.DataBindingUtil
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.topface.billing.ninja.NinjaAddCardActivity
import com.topface.billing.ninja.ThreeDSecureParams
import com.topface.framework.utils.Debug
import com.topface.topface.R
import com.topface.topface.databinding.LayoutNinja3dSecureFragmentBinding
import com.topface.topface.ui.fragments.BaseFragment
import org.jetbrains.anko.layoutInflater

/**
 * Фрагмент для проведения 3DSecure валидации транзакции PN платежей
 * Created by ppavlik on 25.04.17.
 */
class ThreeDSecureFragment : BaseFragment() {
    companion object {
        const val TAG = "ThreeDSecureFragment.TAG"
        const val EXTRA_SETTINGS = "ThreeDSecureFragment.Extra.Settings"

        fun newInstance(settings: ThreeDSecureParams) = ThreeDSecureFragment().apply {
            arguments = Bundle().apply { putParcelable(EXTRA_SETTINGS, settings) }
        }

        fun newInstance(bundle: Bundle) = newInstance(bundle.getParcelable<ThreeDSecureParams>(EXTRA_SETTINGS))
    }

    private val mBinding by lazy {
        DataBindingUtil.inflate<LayoutNinja3dSecureFragmentBinding>(context.layoutInflater, R.layout.layout_ninja_3d_secure_fragment, null, false)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            mBinding.apply {
                val settings = arguments.getParcelable<ThreeDSecureParams>(EXTRA_SETTINGS)
                with(webView) {
                    this.settings.javaScriptEnabled = true
                    setVerticalScrollbarOverlay(true)
                    isVerticalFadingEdgeEnabled = true
                    postUrl(settings.acsUrl, ("PaReq=" + settings.PaReq + "&MD=" + settings.MD + "&TermUrl=" + settings.termUrl).toByteArray())
                    setWebViewClient(PaymentwallClient(this, object : PaymentwallClientInterface {

                        override fun onPageFinished(url: String) {
                            checkLink(settings.paymentSuccessUrl, settings.paymentFailUrl, url)
                        }

                        override fun onPageStarted(url: String) {
                            checkLink(settings.paymentSuccessUrl, settings.paymentFailUrl, url)
                        }

                        override fun onReceivedError(request: WebResourceRequest?, error: WebResourceError?) {
                            finishWithFail()
                        }

                    }))
                }

            }.root

    private fun checkLink(paymentSuccessUrl: String, paymentFailUrl: String, currentLink: String) {
        if (currentLink == paymentSuccessUrl) {
            finishWithSuccess()
        } else if (currentLink == paymentFailUrl) {
            finishWithSuccess()
        }
    }

    private fun finishWithFail() {
        Debug.error("3DS fail")
        finishWithResult(Activity.RESULT_CANCELED)
    }

    private fun finishWithSuccess() {
        Debug.error("3DS success")
        finishWithResult(Activity.RESULT_OK,
                Intent().apply { putExtra(NinjaAddCardActivity.CARD_SENDED_SUCCESFULL, true) })
    }

    fun finishWithResult(resultCode: Int, data: Intent = Intent()) {
        with(activity) {
            setResult(resultCode, data)
            finish()
        }
    }

    private class PaymentwallClient(webView: WebView, private val mPaymentwallClientInterface: PaymentwallClientInterface?) : WebViewClient() {

        init {
            setLayerType(webView)
        }

        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        private fun setLayerType(webView: WebView) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                webView.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null)
            }
        }

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            url?.let { mPaymentwallClientInterface?.onPageStarted(it) }
        }

        override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
            super.onReceivedError(view, request, error)
            mPaymentwallClientInterface?.onReceivedError(request, error)
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            url?.let { mPaymentwallClientInterface?.onPageFinished(it) }
        }
    }

    private interface PaymentwallClientInterface {

        fun onPageStarted(url: String)

        fun onPageFinished(url: String)

        fun onReceivedError(request: WebResourceRequest?, error: WebResourceError?)
    }
}