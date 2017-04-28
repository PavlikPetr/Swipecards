package com.topface.billing.ninja.fragments.three_d_secure

import android.graphics.Bitmap
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient

/**
 * Кастомизированый webView клиент для PN
 * Created by ppavlik on 28.04.17.
 */
class PaymentNinjaWebViewClient(private val mCallback: IPaymentNinjaWebViewClient?) : WebViewClient() {

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        url?.let { mCallback?.onPageStarted(it) }
    }

    override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
        super.onReceivedError(view, request, error)
        mCallback?.onReceivedError(request, error)
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        url?.let { mCallback?.onPageFinished(it) }
    }
}