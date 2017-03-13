package com.topface.topface.ui.settings.payment_ninja

import com.topface.topface.ui.new_adapter.enhanced.ITypeProvider

/**
 * Type provider for users cards and subscriptions
 * Created by ppavlik on 06.03.17.
 */
class SettingsPaymentNinjaTypeProvider : ITypeProvider {
    override fun getType(java: Class<*>) = when (java) {
        CardInfo::class.java -> 1
        SubscriptionInfo::class.java -> 2
        PaymentNinjaHelp::class.java -> 3
        PaymnetNinjaPurchasesLoader::class.java -> 4
        else -> 0
    }
}