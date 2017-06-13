package com.topface.billing

import org.onepf.oms.appstore.googleUtils.Purchase

/**
 * Created by ppavlik on 08.06.17.
 * Инетрфейс для покупок через OpenIab
 */
interface IBilling {
    fun onPurchased(product: Purchase)

    fun onSubscriptionSupported()

    fun onSubscriptionUnsupported()

    fun onInAppBillingSupported()

    fun onInAppBillingUnsupported()
}