package com.topface.billing.ninja.fragments.three_d_secure

import android.webkit.WebResourceError
import android.webkit.WebResourceRequest

/**
 * Интерфейс, который сообщает об изменениях в WebView
 * Created by ppavlik on 28.04.17.
 */
interface IPaymentNinjaWebViewClient {
    fun onPageStarted(url: String)

    fun onPageFinished(url: String)

    fun onReceivedError(request: WebResourceRequest?, error: WebResourceError?)
}